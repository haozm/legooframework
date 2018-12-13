package com.legooframework.model.membercare.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.CRUD;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpcomingTaskEntity extends TaskStatusSupportEntity implements BatchSetter {

    private final TaskType taskType;
    private final Integer memberId, storeId, companyId;
    private final boolean crossStore, automatic;
    private Integer sourceId;
    private List<Integer> mergeInfo;
    private Integer serviceUserId;
    private CRUD crudTag;
    private UpcomingTaskDetailList taskDetails;
    // 扩展信息
    private final LocalDateTime saleDate;

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getId());
        ps.setObject(2, this.taskType.getValue());
        ps.setObject(3, this.getTaskStatus().getStatus());
        ps.setObject(4, this.automatic ? 1 : 0);
        ps.setObject(5, this.crossStore ? 1 : 0);
        ps.setObject(6, this.memberId);
        ps.setObject(7, this.storeId);
        ps.setObject(8, this.companyId);
        ps.setObject(9, this.sourceId);
        ps.setObject(10, Joiner.on(',').join(mergeInfo));
        ps.setObject(11, serviceUserId);
        ps.setObject(12, this.getTenantId());
    }

    private UpcomingTaskEntity(TaskType taskType, boolean automatic, CrmMemberEntity member, CrmStoreEntity store,
                               Integer sourceId, List<Integer> mergeInfo, LocalDateTime saleDate, Integer serviceUserId) {
        super(UUID.randomUUID().toString(), member.getCompanyId().longValue(), 0L);
        Preconditions.checkNotNull(member, "会员信息不可以为空...");
        this.taskType = taskType;
        this.automatic = automatic;
        this.memberId = member.getId();
        this.storeId = store == null ? member.getStoreId() : store.getId();
        this.crossStore = member.isCrossStore(store);
        this.companyId = member.getCompanyId();
        this.serviceUserId = serviceUserId;
        this.sourceId = sourceId;
        this.mergeInfo = Lists.newArrayList(mergeInfo);
        this.crudTag = CRUD.C;
        this.saleDate = saleDate;
    }

    UpcomingTaskEntity(String id, ResultSet res, TaskStatus taskStatus) {
        super(id, res, taskStatus);
        try {
            this.taskType = TaskType.parse(ResultSetUtil.getObject(res, "taskType", Integer.class));
            this.memberId = ResultSetUtil.getObject(res, "memberId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.crossStore = ResultSetUtil.getObject(res, "crossed", Integer.class) == 1;
            this.automatic = ResultSetUtil.getObject(res, "automatic", Integer.class) == 1;
            this.sourceId = ResultSetUtil.getObject(res, "sourceId", Integer.class);
            this.serviceUserId = ResultSetUtil.getOptObject(res, "serviceUserId", Integer.class).orElse(null);
            this.crudTag = CRUD.R;
            this.saleDate = LocalDateTime.fromDateFields(ResultSetUtil.getObject(res, "saleDate", Date.class));
            int task_status = ResultSetUtil.getObject(res, "taskStatus", Integer.class);
            setTaskStatus(TaskStatus.paras(task_status));
            String _mergeInfo = ResultSetUtil.getString(res, "mergeInfo");
            this.mergeInfo = Stream.of(StringUtils.split(_mergeInfo, ',')).map(Integer::valueOf)
                    .collect(Collectors.toList());
            String details = ResultSetUtil.getString(res, "details");
            this.taskDetails = new UpcomingTaskDetailList(this, details);
        } catch (SQLException e) {
            throw new RuntimeException("Restore UpcomingTaskEntity has SQLException", e);
        }
    }

    public List<UpcomingTaskDetailEntity> getTaskDetails() {
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
                saleRecord.getId(), mergeInfo, saleRecord.getSaleDate(), serviceUserId);
    }

//    UpcomingTaskEntity createFestivalDayJob(boolean automatic, CrmMemberEntity member, LocalDateTime startDate,
//                                            LocalDateTime endDate, LocalDate festivalDay) {
//        return new UpcomingTaskEntity(TaskType.Touche90, automatic, member, null, startDate, endDate,
//                festivalDay.toString("yyyy-MM-dd"));
//    }


    public boolean isOwner(CrmStoreEntity store, CrmMemberEntity member) {
        return this.memberId.equals(member.getId()) && this.storeId.equals(store.getId());
    }

    public boolean isCRUD4Insert() {
        return CRUD.C == crudTag;
    }

    public boolean isCRUD4Reader() {
        return CRUD.R == crudTag;
    }

    public boolean isCRUD4Update() {
        return CRUD.U == crudTag;
    }

    void addMergeInfo(Integer anyId) {
        if (this.mergeInfo.contains(anyId)) return;
        this.mergeInfo.add(anyId);
        if (isCRUD4Reader()) this.crudTag = CRUD.U;
    }

    Optional<UpcomingTaskEntity> canceled() {
        if (isCanceled() || isFinished() || isStoped()) return Optional.empty();
        UpcomingTaskEntity clone = (UpcomingTaskEntity) cloneMe();
        List<UpcomingTaskDetailEntity> cancel_list = Lists.newArrayList();
        clone.taskDetails.getDelegate().forEach(detail -> detail.canceled().ifPresent(cancel_list::add));
        clone.taskDetails = new UpcomingTaskDetailList(cancel_list);
        if (clone.isCRUD4Reader()) clone.crudTag = CRUD.U;
        return Optional.of(clone);
    }

    Optional<UpcomingTaskEntity> finished(String detailId) {
        Optional<UpcomingTaskDetailEntity> task_detail_opt = this.taskDetails.findById(detailId);
        Preconditions.checkState(task_detail_opt.isPresent(), "不存在对应的任务...");
        task_detail_opt = task_detail_opt.get().finished();
        if (!task_detail_opt.isPresent()) return Optional.empty();
        UpcomingTaskEntity clone = (UpcomingTaskEntity) cloneMe();
        List<UpcomingTaskDetailEntity> finished_list = Lists.newArrayList();
        finished_list.add(task_detail_opt.get());
        clone.taskDetails = new UpcomingTaskDetailList(finished_list);
        if (this.taskDetails.isLastStep(task_detail_opt.get())) {
            clone.setTaskStatus(TaskStatus.Finished);
            clone.crudTag = CRUD.U;
        }
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> parms = super.toParamMap(excludes);
        parms.put("taskStatus", getTaskStatus().getStatus());
        parms.put("taskType", taskType.getValue());
        parms.put("crossStore", crossStore ? 0 : 1);
        parms.put("automatic", automatic ? 1 : 0);
        return parms;
    }

    Integer getSourceId() {
        return sourceId;
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

    public boolean isExceptioned() {
        return getTaskStatus() == TaskStatus.Exceptioned;
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
                .add("taskStatus", getTaskStatus())
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
