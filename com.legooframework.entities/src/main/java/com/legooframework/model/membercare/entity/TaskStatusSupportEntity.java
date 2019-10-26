package com.legooframework.model.membercare.entity;

import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;
import java.util.Objects;

abstract class TaskStatusSupportEntity extends BaseEntity<Integer> {

    private TaskStatus taskStatus;

    TaskStatusSupportEntity(Integer id, Long tenantId) {
        super(id, tenantId, -1L);
        this.taskStatus = TaskStatus.Create;
    }

    // FOR DB
    TaskStatusSupportEntity(Integer id, ResultSet res) {
        super(id, res);
        this.taskStatus = null;
    }

    protected TaskStatusSupportEntity(Integer id, ResultSet res, Long creator) {
        super(id, res, creator);
    }

    boolean isCreated() {
        return getTaskStatus() == TaskStatus.Create;
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

    boolean isExtensioned() {
        return getTaskStatus() == TaskStatus.Extensioned;
    }

    void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public boolean canExec() {
        return this.taskStatus == TaskStatus.Starting || this.taskStatus == TaskStatus.Extensioned ||
                this.taskStatus == TaskStatus.Create;
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
