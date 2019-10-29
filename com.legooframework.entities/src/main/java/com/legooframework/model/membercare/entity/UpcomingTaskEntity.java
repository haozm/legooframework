package com.legooframework.model.membercare.entity;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

public class UpcomingTaskEntity extends BaseEntity<Integer> implements BatchSetter {

    private final BusinessType businessType;
    private final Integer memberId, storeId, companyId;
    private final boolean crossStore;
    private final String ruleId, categories;
    private Integer sourceId;
    private Integer serviceUserId;
    private UpcomingTaskDetails taskDetails;
    // 90 扩展信息
    private final LocalDateTime saleDate;
    private List<Integer> mergeInfo;
    private final BigDecimal saleTotalAmount;

    private static Joiner JOINER = Joiner.on(',');
    private static Splitter SPLITTER = Splitter.on(',');
    private static Splitter.MapSplitter SPLITTER_MAP = Splitter.on(';').withKeyValueSeparator('=');
    private boolean newData = false;

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getId());
        ps.setObject(2, this.ruleId);
        ps.setObject(3, this.businessType.toString());
        ps.setObject(4, this.categories);
        ps.setObject(5, this.crossStore ? 1 : 0);
        ps.setObject(6, this.memberId);
        ps.setObject(7, this.storeId);
        ps.setObject(8, this.companyId);
        ps.setObject(9, this.sourceId);
        ps.setObject(10, parseMergeInfo());
        ps.setObject(11, serviceUserId);
        ps.setObject(12, this.getTenantId());
    }

    private UpcomingTaskEntity(TaskCareRuleEntity taskCareRule, CrmMemberEntity member, CrmStoreEntity store,
                               Integer sourceId, List<Integer> saleIds, LocalDateTime saleDate,
                               Integer serviceUserId, boolean newData, BigDecimal saleTotalAmount) {
        super(UUID.randomUUID().toString().hashCode(), member.getCompanyId().longValue(), 0L);
        Preconditions.checkNotNull(member, "会员信息不可以为空...");
        this.businessType = taskCareRule.getBusinessType();
        this.ruleId = taskCareRule.getId();
        this.categories = taskCareRule.getCategories();
        this.memberId = member.getId();
        this.storeId = store == null ? member.getStoreId() : store.getId();
        this.crossStore = member.isCrossStore(store);
        this.companyId = member.getCompanyId();
        this.serviceUserId = serviceUserId;
        this.mergeInfo = saleIds;
        this.saleDate = saleDate;
        this.sourceId = sourceId;
        this.newData = newData;
        this.saleTotalAmount = saleTotalAmount;
    }

    UpcomingTaskEntity(Integer id, ResultSet res) {
        super(id, res);
        try {
            this.businessType = BusinessType.parse(ResultSetUtil.getString(res, "businessType"));
            this.memberId = ResultSetUtil.getObject(res, "memberId", Integer.class);
            this.ruleId = ResultSetUtil.getObject(res, "ruleId", String.class);
            this.categories = ResultSetUtil.getString(res, "categories");
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.crossStore = ResultSetUtil.getObject(res, "crossed", Integer.class) == 1;
            this.sourceId = ResultSetUtil.getObject(res, "sourceId", Integer.class);
            this.serviceUserId = ResultSetUtil.getOptObject(res, "serviceUserId", Integer.class).orElse(null);
            String mergeInfo = ResultSetUtil.getString(res, "mergeInfo");
            if (this.businessType == BusinessType.TOUCHED90 && StringUtils.isNotEmpty(mergeInfo)) {
                Map<String, String> params = SPLITTER_MAP.split(mergeInfo);
                String saleDate = MapUtils.getString(params, "saleDate");
                String saleList = MapUtils.getString(params, "saleList");
                this.mergeInfo = SPLITTER.splitToList(saleList).stream().map(Integer::valueOf).collect(Collectors.toList());
                this.saleDate = Strings.isNullOrEmpty(saleDate) ? null : DateTimeUtils.parseYYYYMMDDHHMMSS(saleDate);
                this.saleTotalAmount = new BigDecimal(MapUtils.getLong(params, "saleTotalAmount", 0L));
            } else {
                this.mergeInfo = null;
                this.saleDate = null;
                this.saleTotalAmount = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore UpcomingTaskEntity has SQLException", e);
        }
    }

    String parseMergeInfo() {
        if (this.businessType == BusinessType.TOUCHED90) {
            return String.format("saleTotalAmount=%s;saleDate=%s;saleList=%s", saleTotalAmount, saleDate.toString("yyyyMMddHHmmss"),
                    JOINER.join(mergeInfo));
        }
        return null;
    }

    String getRuleId() {
        return ruleId;
    }

    String getCategories() {
        return categories;
    }

    BusinessType getBusinessType() {
        return businessType;
    }

    List<UpcomingTaskDetailEntity> getTaskDetails() {
        return taskDetails;
    }

    void setTaskDetails(UpcomingTaskDetails taskDetails) {
        this.taskDetails = taskDetails;
    }

    List<Integer> getMergeInfo() {
        return mergeInfo;
    }

    public BigDecimal getSaleTotalAmount() {
        return saleTotalAmount;
    }

//    static UpcomingTaskEntity createJobInstance(TaskCareRuleEntity taskCareRule, CrmMemberEntity member,
//                                                CrmStoreEntity store, SaleRecordEntity saleRecord) {
//        List<Integer> mergeInfo = Lists.newArrayList();
//        mergeInfo.add(saleRecord.getId());
//        Integer serviceUserId = saleRecord.getShoppingguideId().orElse(null);
//        return new UpcomingTaskEntity(taskCareRule, member, store,
//                saleRecord.getId(), mergeInfo, saleRecord.getSaleDate(), serviceUserId, true, saleRecord.getSaleTotalAmount());
//    }

    boolean isOwner(CrmStoreEntity store, CrmMemberEntity member) {
        return this.memberId.equals(member.getId()) && this.storeId.equals(store.getId());
    }

    Optional<UpcomingTaskEntity> addMergeInfo(Integer anyId) {
        if (this.mergeInfo.contains(anyId)) return Optional.empty();
        if (newData) {
            this.mergeInfo.add(anyId);
            return Optional.empty();
        }
        UpcomingTaskEntity clone = (UpcomingTaskEntity) cloneMe();
        clone.mergeInfo.add(anyId);
        clone.taskDetails = null;
        return Optional.of(clone);
    }

    Optional<UpcomingTaskEntity> canceled() {
        if (newData) {
            this.taskDetails.forEach(UpcomingTaskDetailEntity::canceledSelf);
            return Optional.empty();
        }
        List<UpcomingTaskDetailEntity> cancel_list = Lists.newArrayList();
        this.taskDetails.forEach(detail -> detail.makeCanceled().ifPresent(cancel_list::add));
        if (CollectionUtils.isEmpty(cancel_list)) return Optional.empty();
        UpcomingTaskEntity clone = (UpcomingTaskEntity) cloneMe();
        // TODO
        // clone.taskDetails = new UpcomingTaskDetailList(cancel_list);
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> parms = super.toParamMap(excludes);
        parms.put("businessType", businessType.toString());
        parms.put("crossStore", crossStore ? 1 : 0);
        return parms;
    }

    LocalDateTime getSaleDate() {
        return saleDate;
    }

    public boolean isBrithDayJob() {
        return this.businessType == BusinessType.BIRTHDAYTOUCH;
    }

    public boolean isTouche90Job() {
        return this.businessType == BusinessType.TOUCHED90;
    }

    public boolean isCrossStore() {
        return crossStore;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpcomingTaskEntity)) return false;
        if (!super.equals(o)) return false;
        UpcomingTaskEntity that = (UpcomingTaskEntity) o;
        return crossStore == that.crossStore &&
                businessType == that.businessType &&
                Objects.equals(memberId, that.memberId) &&
                Objects.equals(serviceUserId, that.serviceUserId) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(mergeInfo, that.mergeInfo) &&
                Objects.equals(taskDetails, that.taskDetails) &&
                Objects.equals(saleDate, that.saleDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), businessType, memberId, serviceUserId, storeId, companyId, crossStore,
                sourceId, mergeInfo, taskDetails, saleDate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("businessType", businessType)
                .add("memberId", memberId)
                .add("serviceUserId", serviceUserId)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("crossStore", crossStore)
                .add("sourceId", sourceId)
                .add("taskDetails", taskDetails)
                .toString();
    }
}
