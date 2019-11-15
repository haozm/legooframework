package com.legooframework.model.httpproxy.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringJoiner;

public class FusingCountEntity extends BaseEntity<Long> implements BatchSetter {

    private final String module;
    private final String path;
    private final String query;
    private final String timestamp;
    private int fusingCount = 1;

    FusingCountEntity(HttpGateWayEntity gateWay, HttpRequestDto requestDto) {
        super(0L);
        this.module = gateWay.getId();
        this.path = requestDto.getUriComponents().getPath();
        this.query = requestDto.getUriComponents().getQuery();
        this.timestamp = LocalDateTime.now().toString("yyyyMMddHHmm00");
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, module);
        ps.setObject(2, path);
        ps.setObject(3, timestamp);
        ps.setObject(4, query);
        ps.setObject(5, fusingCount);
    }

    void increment() {
        fusingCount += 1;
    }

    String getCacheKey() {
        StringJoiner joiner = new StringJoiner("-");
        return joiner.add(module).add(path).add(timestamp).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FusingCountEntity that = (FusingCountEntity) o;
        return Objects.equal(module, that.module) &&
                Objects.equal(path, that.path) &&
                Objects.equal(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(module, path, timestamp);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("module", module)
                .add("path", path)
                .add("query", query)
                .add("timestamp", timestamp)
                .add("fusingCount", fusingCount)
                .toString();
    }
}
