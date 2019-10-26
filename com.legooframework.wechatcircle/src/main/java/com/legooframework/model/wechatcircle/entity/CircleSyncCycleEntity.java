package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class CircleSyncCycleEntity extends BaseEntity<String> {

    // 1：批量  2 个人
    private final int syncType;
    private long startTime;
    private LocalDateTime startDate;
    private long lastTime;
    private LocalDateTime lastDate;

    private CircleSyncCycleEntity(String id, int syncType, long startTime, long lastTime) {
        super(id);
        this.syncType = syncType;
        this.startTime = startTime;
        this.startDate = DateTimeUtils.unixToLocalDateTime(startTime);
        this.lastTime = lastTime;
        this.lastDate = DateTimeUtils.unixToLocalDateTime(lastTime);
    }

    CircleSyncCycleEntity(String wxId, ResultSet res) {
        super(wxId);
        try {
            this.syncType = res.getInt("syncType");
            this.startTime = res.getLong("startTime");
            this.startDate = DateTimeUtils.unixToLocalDateTime(startTime);
            this.lastTime = res.getLong("lastTime");
            this.lastDate = DateTimeUtils.unixToLocalDateTime(lastTime);
        } catch (SQLException e) {
            throw new RuntimeException("Restore CircleSyncCycleEntity has SQLException", e);
        }
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("syncType", syncType == 1 ? "batch" : "single");
        params.put("startTime", startTime);
        params.put("lastTime", lastTime);
        return params;
    }

    public static CircleSyncCycleEntity createBatchCycle(String wxId, long startTime, long lastTime) {
        return new CircleSyncCycleEntity(wxId, 1, startTime, lastTime);
    }

    public static CircleSyncCycleEntity createSingleCycle(String wxId, long startTime, long lastTime) {
        return new CircleSyncCycleEntity(wxId, 2, startTime, lastTime);
    }

    int getSyncType() {
        return syncType;
    }

    Optional<CircleSyncCycleEntity> changeLastTime(CircleSyncCycleEntity that) {
        if (that.lastTime <= this.lastTime) return Optional.empty();
        CircleSyncCycleEntity clone = (CircleSyncCycleEntity) cloneMe();
        clone.lastTime = that.lastTime;
        return Optional.of(clone);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CircleSyncCycleEntity that = (CircleSyncCycleEntity) o;
        return Objects.equal(this.getId(), that.getId()) &&
                syncType == that.syncType &&
                startTime == that.startTime &&
                lastTime == that.lastTime;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("syncType", syncType);
        params.put("startTime", startTime);
        params.put("startDate", startDate.toDate());
        params.put("lastTime", lastTime);
        params.put("lastDate", lastDate.toDate());
        return params;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId(), syncType, startTime, lastTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.getId())
                .add("syncType", this.syncType)
                .add("startTime", startTime)
                .add("lastTime", lastTime)
                .toString();
    }
}
