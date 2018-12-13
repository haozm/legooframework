package com.legooframework.model.membercare.entity;

import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;
import java.util.Objects;

abstract class TaskStatusSupportEntity extends BaseEntity<String> {

    private TaskStatus taskStatus;

    TaskStatusSupportEntity(String id, Long tenantId, Long creator) {
        super(id, tenantId, creator);
        this.taskStatus = TaskStatus.Create;
    }

    TaskStatusSupportEntity(String id, ResultSet res, TaskStatus taskStatus) {
        super(id, res);
        this.taskStatus = taskStatus;
    }

    boolean isCreated() {
        return getTaskStatus() == TaskStatus.Canceled;
    }

    boolean isStarting() {
        return getTaskStatus() == TaskStatus.Starting;
    }

    boolean isFinished() {
        return getTaskStatus() == TaskStatus.Finished;
    }

    boolean isStoped() {
        return getTaskStatus() == TaskStatus.Stoped;
    }

    boolean isCanceled() {
        return getTaskStatus() == TaskStatus.Canceled;
    }

    boolean isExpired() {
        return getTaskStatus() == TaskStatus.Expired;
    }

    void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    TaskStatus getTaskStatus() {
        return taskStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskStatusSupportEntity)) return false;
        TaskStatusSupportEntity that = (TaskStatusSupportEntity) o;
        return taskStatus == that.taskStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskStatus);
    }
}
