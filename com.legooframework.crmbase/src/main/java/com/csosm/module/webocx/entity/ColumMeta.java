package com.csosm.module.webocx.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class ColumMeta {

    private final String id, name, type;
    private final boolean fixed;

    ColumMeta(String id, String name, String type, boolean fixed) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.fixed = fixed;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isFixed() {
        return fixed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumMeta columMeta = (ColumMeta) o;
        return fixed == columMeta.fixed &&
                Objects.equal(id, columMeta.id) &&
                Objects.equal(name, columMeta.name) &&
                Objects.equal(type, columMeta.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, type, fixed);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("type", type)
                .add("fixed", fixed)
                .toString();
    }
}
