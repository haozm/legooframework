package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.CRUD;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UpcomingTaskDetailEntity extends TaskStatusSupportEntity implements BatchSetter {

    private final String taskId;
    private final LocalDateTime startDateTime, expiredDateTime;
    private LocalDateTime finishedDateTime;
    private CRUD crudTag;

    UpcomingTaskDetailEntity(UpcomingTaskEntity taskEntity, LocalDateTime startDateTime, LocalDateTime expiredDateTime) {
        super(UUID.randomUUID().toString(), 100000L, -1L);
        this.startDateTime = startDateTime;
        this.expiredDateTime = expiredDateTime;
        this.crudTag = CRUD.C;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiredDateTime)) {
            setTaskStatus(TaskStatus.Expired);
        } else if (now.isAfter(startDateTime)) {
            setTaskStatus(TaskStatus.Starting);
        }
        this.taskId = taskEntity.getId();
        this.finishedDateTime = null;
    }

    // for DB
    UpcomingTaskDetailEntity(String id, UpcomingTaskEntity taskEntity, TaskStatus taskStatus,
                             LocalDateTime startDateTime, LocalDateTime expiredDateTime, LocalDateTime finishedDateTime) {
        super(id, 100000L, -1L);
        this.startDateTime = startDateTime;
        this.expiredDateTime = expiredDateTime;
        setTaskStatus(taskStatus);
        this.taskId = taskEntity.getId();
        this.crudTag = CRUD.R;
        this.finishedDateTime = finishedDateTime;
    }

    Optional<UpcomingTaskDetailEntity> canceled() {
        if (isCanceled() || isExpired() || isFinished() || isStoped()) return Optional.empty();
        if (super.isCreated() || isStarting()) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Canceled);
            if (this.crudTag == CRUD.R) this.crudTag = CRUD.U;
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    Optional<UpcomingTaskDetailEntity> finished() {
        Preconditions.checkState(isCanceled(), "当前任务节点已经被取消，无法完成...");
        if (isFinished()) return Optional.empty();
        if (isCreated() || isStarting()) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Finished);
            clone.finishedDateTime = LocalDateTime.now();
            clone.setEditTime(DateTime.now());
            if (this.crudTag == CRUD.R) this.crudTag = CRUD.U;
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    LocalDateTime getFinishedDateTime() {
        return finishedDateTime;
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

    LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getId());
        ps.setObject(2, this.taskId);
        ps.setObject(3, this.getTaskStatus().getStatus());
        ps.setObject(4, this.startDateTime.toDate());
        ps.setObject(5, this.expiredDateTime.toDate());
        ps.setObject(6, this.getCreateTime().toDate());
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
                .add("finishedDateTime", finishedDateTime)
                .toString();
    }
}
