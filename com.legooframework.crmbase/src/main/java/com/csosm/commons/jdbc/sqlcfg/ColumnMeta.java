package com.csosm.commons.jdbc.sqlcfg;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class ColumnMeta {

    private final String id, name, desc, type, showType, sublist;
    private final boolean fixed, freeze, sort, category, legend, sum;
    private final List<ColumnMeta> children;
    private final Map<String, String> addDatas;

    public ColumnMeta(String id, String name, String desc, String type, boolean fixed, String showType,
                      boolean sorted, boolean category, boolean legend, boolean freeze, boolean sum,
                      String sublist, Map<String, String> addDatas) {
        this.id = id;
        this.name = name;
        this.sum = sum;
        this.desc = desc;
        this.type = type;
        this.fixed = fixed;
        this.showType = showType;
        this.sort = sorted;
        this.category = category;
        this.freeze = freeze;
        this.legend = legend;
        this.sublist = sublist;
        this.children = Lists.newArrayList();
        this.addDatas = addDatas;
    }

    public String getSublist() {
        return sublist;
    }

    public boolean isFreeze() {
        return freeze;
    }

    public boolean isSum() {
        return sum;
    }

    public Map<String, String> getAddDatas() {
        return addDatas;
    }

    public boolean isPercentage() {
        return "percentage".equalsIgnoreCase(type);
    }

    public boolean isSort() {
        return sort;
    }

    public boolean isCategory() {
        return category;
    }

    public boolean isLegend() {
        return legend;
    }

    public boolean isFixed() {
        return fixed;
    }

    public Optional<String> getShowType() {
        return Optional.fromNullable(showType);
    }

    public boolean isMoney() {
        return "money".equalsIgnoreCase(type);
    }

    public boolean isInt() {
        return "int".equalsIgnoreCase(type);
    }

    public boolean isLong() {
        return "long".equalsIgnoreCase(type);
    }

    public List<ColumnMeta> getChildren() {
        return children;
    }

    public boolean isDouble() {
        return "float".equalsIgnoreCase(type) || "double".equalsIgnoreCase(type);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public void addColumnMeta(ColumnMeta columnMeta) {
        this.children.add(columnMeta);
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("desc", desc)
                .add("type", type)
                .add("showType", showType)
                .add("sublist", sublist)
                .add("fixed", fixed)
                .add("freeze", freeze)
                .add("sort", sort)
                .add("category", category)
                .add("legend", legend)
                .add("sum", sum)
                .add("children", children)
                .toString();
    }
}
