package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.util.Objects;

public class GoodsBaseEntity extends BaseEntity<Integer> {

    private final int status;
    private final String function, name;
    private final Integer companyId;

    GoodsBaseEntity(int id, int status, String function, String name, Integer companyId) {
        super(id);
        this.status = status;
        this.function = function;
        this.name = name;
        this.companyId = companyId;
    }

    public String getFunction() {
        return function;
    }

    public int getStatus() {
        return status;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GoodsBaseEntity goodsBase = (GoodsBaseEntity) o;
        return status == goodsBase.status &&
                Objects.equals(function, goodsBase.function) &&
                Objects.equals(name, goodsBase.name) &&
                Objects.equals(companyId, goodsBase.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, function, name, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("status", status)
                .add("function", function)
                .add("name", name)
                .add("companyId", companyId)
                .toString();
    }
}
