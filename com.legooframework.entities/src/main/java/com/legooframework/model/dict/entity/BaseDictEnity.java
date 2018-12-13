package com.legooframework.model.dict.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseDictEnity extends BaseEntity<Integer> {

    private final String type; // 字典分类

    BaseDictEnity(String type, Long tenantId, Long creator) {
        super(0, tenantId, creator);
        this.type = type;
    }

    BaseDictEnity(int id, ResultSet res) {
        super(id, res);
        try {
            this.type = ResultSetUtil.getString(res, "type");
        } catch (SQLException e) {
            throw new RuntimeException("Restore BaseDictEnity has SQLException", e);
        }
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseDictEnity that = (BaseDictEnity) o;
        return Objects.equal(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .toString();
    }
}
