package com.legooframework.model.membercare.entity;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

public class UpcomingTaskEntity extends BaseEntity<String> implements BatchSetter {

    private final TaskType taskType;
    private final Integer memberId, storeId, companyId;
    private final boolean crossStore, automatic;
    private Integer sourceId;
    private Integer serviceUserId;
    private UpcomingTaskDetailList taskDetails;
    // 90 扩展信息
    private final LocalDateTime saleDate;
    private List<Integer> mergeInfo;

    private static Joiner JOINER = Joiner.on(',');
    private static Splitter SPLITTER = Splitter.on(',');
    private static Splitter.MapSplitter SPLITTER_MAP = Splitter.on(';').withKeyValueSeparator('=');
    private boolean newData = false;

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getId());
        ps.setObject(2, this.taskType.getValue());
        ps.setObject(3, this.automatic ? 1 : 0);
        ps.setObject(4, this.crossStore ? 1 : 0);
        ps.setObject(5, this.memberId);
        ps.setObject(6, this.storeId);
        ps.setObject(7, this.companyId);
        ps.setObject(8, this.sourceId);
        ps.setObject(9, getContext());
        ps.setObject(10, serviceUserId);
        ps.setObject(11, this.getTenantId());
    }

    private UpcomingTaskEntity(TaskType taskType, boolean automatic, CrmMemberEntity member, CrmStoreEntity store,
                               Integer sourceId, List<Integer> saleIds, LocalDateTime saleDate,
                               Integer serviceUserId, boolean newData) {
        super(UUID.randomUUID().toString(), member.getCompanyId().longValue(), 0L);
        Preconditions.checkNotNull(member, "会员信息不可以为空...");
        this.taskType = taskType;
        this.automatic = automatic;
        this.memberId = member.getId();
        this.storeId = store == null ? member.getStoreId() : store.getId();
        this.crossStore = member.isCrossStore(store);
        this.companyId = member.getCompanyId();
        this.serviceUserId = serviceUserId;
        this.mergeInfo = saleIds;
        this.saleDate = saleDate;
        this.sourceId = sourceId;
        this.newData = newData;
    }

    UpcomingTaskEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.taskType = TaskType.parse(ResultSetUtil.getObject(res, "taskType", Integer.class));
            this.memberId = ResultSetUtil.getObject(res, "memberId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.crossStore = ResultSetUtil.getObject(res, "crossed", Integer.class) == 1;
            this.automatic = ResultSetUtil.getObject(res, "automatic", Integer.class) == 1;
            this.sourceId = ResultSetUtil.getObject(res, "sourceId", Integer.class);
            this.serviceUserId = ResultSetUtil.getOptObject(res, "serviceUserId", Integer.class).orElse(null);
            String context = ResultSetUtil.getString(res, "mergeInfo");
            if (this.taskType == TaskType.Touche90 && StringUtils.isNotEmpty(context)) {
                Map<String, String> params = SPLITTER_MAP.split(context);
                String saleDate = MapUtils.getString(params, "saleDate");
                String saleList = MapUtils.getString(params, "saleList");
                this.mergeInfo = SPLITTER.splitToList(saleList).stream().map(Integer::valueOf).collect(Collectors.toList());
                this.saleDate = Strings.isNullOrEmpty(saleDate) ? null : DateTimeUtils.parseYYYYMMDDHHMMSS(saleDate);
            } else {
                this.mergeInfo = null;
                this.saleDate = null;
            }
            String details = ResultSetUtil.getString(res, "details");
            this.taskDetails = new UpcomingTaskDetailList(this, details);
        } catch (SQLException e) {
            throw new RuntimeException("Restore UpcomingTaskEntity has SQLException", e);
        }
    }

    public String getContext() {
        if (this.taskType == TaskType.Touche90) {
            return String.format("saleDate=%s;saleList=%s", saleDate.toString("yyyyMMddHHmmss"), JOINER.join(mergeInfo));
        }
        return null;
    }

    List<UpcomingTaskDetailEntity> getTaskDetails() {
        return taskDetails.getDelegate();
    }

    void setTaskDetails(UpcomingTaskDetailList taskDetails) {
        this.taskDetails = taskDetails;
    }

//    UpcomingTaskEntity createBrithDayJob(boolean automatic, CrmMemberEntity member, LocalDateTime startDate,
//                                         LocalDateTime endDate) {
//        Preconditions.checkState(member.getBirthday().isPresent(), "会员生日不可以为空...");
//        return new UpcomingTaskEntity(TaskType.BrithDay, automatic, member, null, startDate, endDate,
//                member.getBirthday().get().toViewString());
//    }

    List<Integer> getMergeInfo() {
        return mergeInfo;
    }

    static UpcomingTaskEntity createTouche90Job(boolean automatic, CrmMemberEntity member, CrmStoreEntity store,
                                                SaleRecordEntity saleRecord) {
        List<Integer> mergeInfo = Lists.newArrayList();
        mergeInfo.add(saleRecord.getId());
        Integer serviceUserId = null;
        if (saleRecord.getServiceShoppingguideIds().isPresent()) {
            serviceUserId = saleRecord.getServiceShoppingguideIds().get().iterator().next();
        } else if (saleRecord.getServiceShoppingguideId().isPresent()) {
            serviceUserId = saleRecord.getServiceShoppingguideId().get();
        }
        return new UpcomingTaskEntity(TaskType.Touche90, automatic, member, store,
                saleRecord.getId(), mergeInfo, saleRecord.getSaleDate(), serviceUserId, true);
    }

//    UpcomingTaskEntity createFestivalDayJob(boolean automatic, CrmMemberEntity member, LocalDateTime startDate,
//                                            LocalDateTime endDate, LocalDate festivalDay) {
//        return new UpcomingTaskEntity(TaskType.Touche90, automatic, member, null, startDate, endDate,
//                festivalDay.toString("yyyy-MM-dd"));
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
            this.taskDetails.getDelegate().forEach(UpcomingTaskDetailEntity::canceledSelf);
            return Optional.empty();
        }
        List<UpcomingTaskDetailEntity> cancel_list = Lists.newArrayList();
        this.taskDetails.getDelegate().forEach(detail -> detail.canceled().ifPresent(cancel_list::add));
        if (CollectionUtils.isEmpty(cancel_list)) return Optional.empty();
        UpcomingTaskEntity clone = (UpcomingTaskEntity) cloneMe();
        clone.taskDetails = new UpcomingTaskDetailList(cancel_list);
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> parms = super.toParamMap(excludes);
        parms.put("taskType", taskType.getValue());
        parms.put("crossStore", crossStore ? 1 : 0);
        parms.put("automatic", automatic ? 1 : 0);
        return parms;
    }

    LocalDateTime getSaleDate() {
        return saleDate;
    }

    public boolean isBrithDayJob() {
        return this.taskType == TaskType.BrithDay;
    }

    public boolean isTouche90Job() {
        return this.taskType == TaskType.Touche90;
    }

    public boolean isFestivalDayJob() {
        return this.taskType == TaskType.FestivalDay;
    }

    public boolean isCrossStore() {
        return crossStore;
    }

    public boolean isAutomatic() {
        return automatic;
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
                automatic == that.automatic &&
                taskType == that.taskType &&
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
        return Objects.hash(super.hashCode(), taskType, memberId, serviceUserId, storeId, companyId, crossStore, automatic,
                sourceId, mergeInfo, taskDetails, saleDate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskType", taskType)
                .add("memberId", memberId)
                .add("serviceUserId", serviceUserId)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("crossStore", crossStore)
                .add("automatic", automatic)
                .add("sourceId", sourceId)
                .add("taskDetails", taskDetails)
                .toString();
    }
}
