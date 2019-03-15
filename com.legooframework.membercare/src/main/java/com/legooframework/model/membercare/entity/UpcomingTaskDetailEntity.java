package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UpcomingTaskDetailEntity extends TaskStatusSupportEntity implements BatchSetter {

    private final String taskId;
    private final LocalDateTime startDateTime, expiredDateTime;
    private LocalDateTime finishedDateTime;
    private String stepIndex;

    UpcomingTaskDetailEntity(UpcomingTaskEntity taskEntity, LocalDateTime startDateTime, LocalDateTime expiredDateTime,
                             String stepIndex) {
        super(UUID.randomUUID().toString(), 100000L, -1L);
        this.startDateTime = startDateTime;
        this.expiredDateTime = expiredDateTime;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiredDateTime)) {
            setTaskStatus(TaskStatus.Expired);
        } else if (now.isAfter(startDateTime)) {
            setTaskStatus(TaskStatus.Starting);
        }
        this.taskId = taskEntity.getId();
        this.stepIndex = stepIndex;
        this.finishedDateTime = null;
    }

    // for DB
    UpcomingTaskDetailEntity(String id, String taskId, TaskStatus taskStatus,
                             LocalDateTime startDateTime, LocalDateTime expiredDateTime,
                             String stepIndex, LocalDateTime finishedDateTime) {
        super(id, 100000L, -1L);
        this.startDateTime = startDateTime;
        this.expiredDateTime = expiredDateTime;
        this.stepIndex = stepIndex;
        setTaskStatus(taskStatus);
        this.taskId = taskId;
        this.finishedDateTime = finishedDateTime;
    }

    public Optional<UpcomingTaskDetailEntity> makeStarting() {
        if (!super.isCreated()) return Optional.empty();
        LocalDateTime now = LocalDateTime.now();
        if (startDateTime.isBefore(now) && expiredDateTime.isAfter(now)) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Starting);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    public Optional<UpcomingTaskDetailEntity> makeExpired() {
        if (!super.isStarting()) return Optional.empty();
        LocalDateTime now = LocalDateTime.now();
        if (expiredDateTime.isBefore(now)) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Expired);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    // for DB
    UpcomingTaskDetailEntity(String id, ResultSet res) {
        super(id, 100000L, -1L);
        try {
            this.startDateTime = DateTimeUtils.parseDef(res.getString("startDate"));
            this.expiredDateTime = DateTimeUtils.parseDef(res.getString("expiredDate"));
            this.stepIndex = res.getString("stepIndex");
            setTaskStatus(TaskStatus.paras(res.getInt("taskStatus")));
            this.taskId = res.getString("taskId");
            String finishedDate = res.getString("finishedDate");
            this.finishedDateTime = finishedDate == null ? null : DateTimeUtils.parseDef(finishedDate);
        } catch (SQLException e) {
            throw new RuntimeException("Restore UpcomingTaskDetailEntity has SQLException", e);
        }

    }

    Optional<UpcomingTaskDetailEntity> canceled() {
        if (isCanceled() || isExpired() || isFinished() || isStoped()) return Optional.empty();
        if (super.isCreated() || isStarting()) {
            UpcomingTaskDetailEntity clone = (UpcomingTaskDetailEntity) cloneMe();
            clone.setTaskStatus(TaskStatus.Canceled);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    void canceledSelf() {
        if (isStarting() || isCreated()) setTaskStatus(TaskStatus.Canceled);
    }

    LocalDateTime getExpiredDateTime() {
        return expiredDateTime;
    }

    Optional<UpcomingTaskDetailEntity> finished() {
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

    LocalDateTime getFinishedDateTime() {
        return finishedDateTime;
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
        ps.setObject(6, this.stepIndex);
        ps.setObject(7, this.getCreateTime().toDate());
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
