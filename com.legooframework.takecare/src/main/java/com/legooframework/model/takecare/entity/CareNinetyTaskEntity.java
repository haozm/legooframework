package com.legooframework.model.takecare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CareNinetyTaskEntity extends BaseEntity<Integer> {
    private final Integer planId, memberId, companyId, storeId, taskType = 1;
    // taskState :任务状态： 1 - 未开始， 2 - 已完成，3 - 执行失败， 4 - 已取消， 5 - 已过期  6 :取消
    private int taskState, taskNode, operateType, messageTempletId;
    private final LocalDateTime createTime, startTime, overdueTime;
    private LocalDateTime doneTime;
    private String sendContent, remark;

    CareNinetyTaskEntity(Integer id, ResultSet resultSet) throws RuntimeException {
        super(id);
        try {
            this.planId = resultSet.getInt("planId");
            this.operateType = resultSet.getInt("operateType");
            this.taskNode = resultSet.getInt("taskNode");
            this.remark = resultSet.getString("remark");
            this.memberId = resultSet.getInt("member_id");
            this.companyId = resultSet.getInt("company_id");
            this.storeId = resultSet.getInt("store_id");
            this.taskState = resultSet.getInt("taskState");
            this.messageTempletId = resultSet.getInt("messageTemplet_id");
            this.sendContent = resultSet.getString("sendContent");
            this.createTime = LocalDateTime.fromDateFields(resultSet.getTimestamp("createTime"));
            this.startTime = LocalDateTime.fromDateFields(resultSet.getTimestamp("startTime"));
            this.overdueTime = LocalDateTime.fromDateFields(resultSet.getTimestamp("overdueTime"));
            this.doneTime = resultSet.getTimestamp("doneTime") == null ? null
                    : LocalDateTime.fromDateFields(resultSet.getTimestamp("doneTime"));
        } catch (SQLException e) {
            throw new RuntimeException("还原对象 CareNinetyDetailEntity 发生异常", e);
        }
    }

    Integer getCompanyId() {
        return companyId;
    }

    Integer getStoreId() {
        return storeId;
    }

    int getTaskNode() {
        return taskNode;
    }

    String getRemark() {
        return remark;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public Optional<Integer> getMessageTempletId() {
        return Optional.ofNullable(messageTempletId == 0 ? null : messageTempletId);
    }

    Integer getPlanId() {
        return planId;
    }

    int getTaskState() {
        return taskState;
    }

    Optional<CareNinetyTaskEntity> canceled() {
        if (isProcessing()) {
            this.taskState = 5;
            this.doneTime = null;
            this.remark = "该节点被取消";
            return Optional.of(this);
        }
        return Optional.empty();
    }

    Optional<CareNinetyTaskEntity> finished() {
        if (isProcessing()) {
            this.taskState = 2;
            this.doneTime = LocalDateTime.now();
            this.remark = "该节点完成执行";
            return Optional.of(this);
        }
        return Optional.empty();
    }

    LocalDateTime getStartTime() {
        return startTime;
    }

    boolean isCanceled() {
        return this.taskState == 5 || this.taskState == 6;
    }

    boolean isProcessing() {
        return !isFinished() && !isCanceled();
    }

    boolean isFinished() {
        return this.taskState == 2;
    }

    boolean isExpiredWitnNoUpdate() {
        return isProcessing() && LocalDateTime.now().isAfter(this.overdueTime);
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskId", getId());
        params.put("planId", this.planId);
        params.put("taskNode", this.taskNode);
        params.put("taskState", this.taskState);
        params.put("taskStateDesc", isProcessing() ? "进行中" : isCanceled() ? "已取消" : isFinished() ? "已完成" : "未知状态");
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CareNinetyTaskEntity that = (CareNinetyTaskEntity) o;
        return taskNode == that.taskNode &&
                Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), planId, taskNode);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("detailId", getId())
                .add("planId", planId)
                .add("taskType", taskType)
                .add("taskState", taskState)
                .add("taskNode", taskNode)
                .add("operateType", operateType)
                .add("messageTempletId", messageTempletId)
                .add("createTime", createTime)
                .add("startTime", startTime)
                .add("overdueTime", overdueTime)
                .add("doneTime", doneTime)
                .add("sendContent", sendContent)
                .add("remark", remark)
                .toString();
    }
}
