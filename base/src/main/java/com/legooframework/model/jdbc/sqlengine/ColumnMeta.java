package com.legooframework.model.jdbc.sqlengine;

import com.google.common.base.MoreObjects;

import java.util.Optional;

public class ColumnMeta {

    private final String id, name, desc, showType;
    private final ColumnType type;
    private final boolean fixed;

    public ColumnMeta(String id, String name, String desc, ColumnType type, boolean fixed, String showType) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.fixed = fixed;
        this.showType = showType;
    }

    public boolean isFixed() {
        return fixed;
    }

    public Optional<String> getShowType() {
        return Optional.ofNullable(showType);
    }

    public boolean isPercentage() {
        return ColumnType.PERCENTAGE == type;
    }

    public boolean isMoney() {
        return ColumnType.RMB == type || ColumnType.MONEY == type;
    }

    public boolean isInt() {
        return ColumnType.INT == type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getDesc() {
        return Optional.ofNullable(desc);
    }

    public ColumnType getType() {
        return type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("desc", desc)
                .add("type", type)
                .toString();
    }
}
