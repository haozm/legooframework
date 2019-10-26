package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UpcomingTaskDetailEntity extends TaskStatusSupportEntity implements BatchSetter {

    private final Integer taskId, memberId;
    private final LocalDateTime startDateTime, expiredDateTime;
    private final BusinessType businessType;
    private final String categories, ruleId, subRuleId;
    private String stepIndex, remarks;
    private final Integer companyId, storeId;
    private LocalDateTime finishedDateTime;
    // 自动执行模式
    private boolean autoRun;
    private LocalTime autoRunTime;

    UpcomingTaskDetailEntity(UpcomingTaskEntity taskEntity, LocalDateTime startDateTime, LocalDateTime expiredDateTime,
                             TaskCareRuleEntity careRule, TaskCareDetailRule touch90Detail) {
        super(UUID.randomUUID().toString().hashCode(), taskEntity.getTenantId());
        this.startDateTime = startDateTime;
        this.ruleId = taskEntity.getRuleId();
        this.expiredDateTime = expiredDateTime;
        this.memberId = taskEntity.getMemberId();
        LocalDateTime now = LocalDateTime.now();
        this.businessType = taskEntity.getBusinessType();
        this.companyId = taskEntity.getCompanyId();
        this.storeId = taskEntity.getStoreId();
        this.categories = taskEntity.getCategories();
        if (now.isAfter(expiredDateTime)) {
            setTaskStatus(TaskStatus.Expired);
        } else if (now.isAfter(startDateTime) || now.equals(startDateTime)) {
            setTaskStatus(TaskStatus.Starting);
        }
        this.taskId = taskEntity.getId();
        this.stepIndex = touch90Detail.getDelay().toString();
        this.subRuleId = touch90Detail.getId();
        this.finishedDateTime = null;
        this.autoRun = touch90Detail.isAuto();
        this.remarks = null;
        if (autoRun) {
            this.autoRunTime = touch90Detail.getStartTime();
        }
    }

    public String getSubRuleId() {
        return subRuleId;
    }

    // for DB
    UpcomingTaskDetailEntity(Integer id, ResultSet res) {
        super(id, res);
        try {
            this.taskId = res.getInt("taskId");
            this.startDateTime = DateTimeUtils.parseDef(res.getString("startDate"));
            this.expiredDateTime = DateTimeUtils.parseDef(res.getString("expiredDate"));
            this.stepIndex = res.getString("stepIndex");
            this.categories = res.getString("categories");
            this.ruleId = res.getString("ruleId");
            this.businessType = BusinessType.parse(res.getString("businessType"));
            setTaskStatus(TaskStatus.paras(res.getInt("taskStatus")));
            String finishedDate = res.getString("finishedDate");
            this.companyId = (int) res.getLong("companyId");
            this.memberId = res.getInt("memberId");
            this.storeId = res.getInt("storeId");
            this.autoRun = ResultSetUtil.getBooleanByInt(res, "autoRun");
            this.subRuleId = res.getString("subRuleId");
            this.remarks = res.getString("remarks");
            this.finishedDateTime = finishedDate == null ? null : DateTimeUtils.parseDef(finishedDate);
            if (this.autoRun) {
                String[] auto_run_time = StringUtils.split(res.getString("autoRunTime"), ':');
                this.autoRunTime = new LocalTime(Integer.valueOf(auto_run_time[0]), Integer.valueOf(auto_run_time[1]), 0);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore UpcomingTaskDetailEntity has SQLException", e);
        }
    }

    public Integer getMemberId() {
        return memberId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getRuleId() {
        return ruleId;
    }

    // 设定任务开始开始执行
    Optional<UpcomingTaskDetailEntity> makeStarting() {
        final LocalDateTime now = LocalDateTime.now();
        if (startDateTime.isBefore(now) && expiredDateTime.isAfter(now)) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Starting);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    // 审定任务发生异常
    public UpcomingTaskDetailEntity makeException(String remarks) {
        UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
        clone.setTaskStatus(TaskStatus.Exceptioned);
        clone.remarks = Strings.isNullOrEmpty(remarks) ? "节点执行异常" : remarks;
        return clone;
    }

    // 任务节点过期设定
    Optional<UpcomingTaskDetailEntity> makeExpired() {
        if (!(super.isCreated() || super.isStarting() || super.isExtensioned())) return Optional.empty();
        LocalDateTime now = LocalDateTime.now();
        if (expiredDateTime.isBefore(now)) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Expired);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    //  任务节点  超期设定
    Optional<UpcomingTaskDetailEntity> makeExtensioned() {
        if (!super.isStarting()) return Optional.empty();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime extensioned_date =
                new LocalDateTime(startDateTime.getYear(), startDateTime.getMonthOfYear(), startDateTime.getDayOfMonth(), 0, 0, 1)
                        .plusDays(1);
        if (now.isAfter(extensioned_date) && expiredDateTime.isAfter(extensioned_date)) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Extensioned);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    // 任务完成
    public Optional<UpcomingTaskDetailEntity> makeFinished() {
        Preconditions.checkState(isCanceled(), "当前任务节点已经被取消，无法完成...");
        if (isFinished()) return Optional.empty();
        if (isCreated() || isStarting()) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Finished);
            clone.finishedDateTime = LocalDateTime.now();
            clone.setEditTime(DateTime.now());
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    //  任务被取消设定
    Optional<UpcomingTaskDetailEntity> makeCanceled() {
        if (super.isCanceled() || super.isExpired() || super.isFinished() || super.isStoped()) return Optional.empty();
        if (super.isCreated() || super.isStarting() || super.isExtensioned()) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Canceled);
            clone.remarks = "任务被取消";
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    //  任务被取消设定
    Optional<UpcomingTaskDetailEntity> makeStoped() {
        if (super.isCanceled() || super.isExpired() || super.isFinished() || super.isStoped()) return Optional.empty();
        if (super.isCreated() || super.isStarting() || super.isExtensioned()) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Stoped);
            clone.remarks = "任务被中止";
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    BusinessType getBusinessType() {
        return businessType;
    }

    void canceledSelf() {
        if (isStarting() || isCreated()) setTaskStatus(TaskStatus.Canceled);
    }

    LocalDateTime getExpiredDateTime() {
        return expiredDateTime;
    }

    public String getCategories() {
        return categories;
    }

    public String getStepIndex() {
        return stepIndex;
    }

    LocalDateTime getFinishedDateTime() {
        return finishedDateTime;
    }

    LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        id, task_id, task_status, start_date, expired_date, step_index, business_type, store_id,
//                member_id, createTime, tenant_id, sub_rule_id, categories, auto_exec
        ps.setObject(1, getId());
        ps.setObject(2, this.taskId);
        ps.setObject(3, this.getTaskStatus().getStatus());
        ps.setObject(4, this.startDateTime.toDate());
        ps.setObject(5, this.expiredDateTime.toDate());
        ps.setObject(6, this.stepIndex);
        ps.setObject(7, this.businessType.toString());
        ps.setObject(8, this.storeId);
        ps.setObject(9, this.memberId);
        ps.setObject(10, this.getCreateTime().toDate());
        ps.setObject(11, this.companyId);
        ps.setObject(12, this.subRuleId);
        ps.setObject(13, this.categories);
        ps.setObject(14, this.ruleId);
        ps.setObject(15, this.autoRun ? 1 : 0);
        ps.setObject(16, this.autoRunTime == null ? null : this.autoRunTime.toString("HH:mm:00"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpcomingTaskDetailEntity)) return false;
        if (!super.equals(o)) return false;
        UpcomingTaskDetailEntity that = (UpcomingTaskDetailEntity) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(startDateTime, that.startDateTime) &&
                Objects.equals(expiredDateTime, that.expiredDateTime) &&
                Objects.equals(finishedDateTime, that.finishedDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), taskId, startDateTime, expiredDateTime, finishedDateTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("startDateTime", startDateTime)
                .add("expiredDateTime", expiredDateTime)
                .add("taskStatus", getTaskStatus())
                .add("autoRun", autoRun)
                .add("autoRunTime", autoRunTime)
                .add("finishedDateTime", finishedDateTime)
                .add("remarks", remarks)
                .toString();
    }
}
