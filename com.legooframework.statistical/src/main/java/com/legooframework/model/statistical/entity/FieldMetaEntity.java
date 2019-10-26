package com.legooframework.model.statistical.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class FieldMetaEntity extends BaseEntity<String> {

    private final String title, type, fmt, drillType,desc;
    private final boolean order;

    private FieldMetaEntity(String id, String title, String type, String fmt, String drillType, boolean order,String desc) {
        super(id);
        this.title = title;
        this.type = type;
        this.order = order;
        this.fmt = fmt;
        this.drillType = drillType;
        this.desc = desc;
    }

    public static FieldMetaEntity create(String id, String title, String type, String fmt, String drillType, boolean order,String desc) {
        return new FieldMetaEntity(id, title, type, fmt, drillType, order,desc);
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("title", title);
        params.put("order", order);
        params.put("type", type);
        params.put("fmt", Strings.nullToEmpty(fmt));
        params.put("drillType", Strings.nullToEmpty(drillType));
        params.put("desc", Strings.nullToEmpty(desc));
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("title", title)
                .add("type", type)
                .add("fmt", fmt)
                .add("order", order)
                .add("drillType", drillType)
                .toString();
    }
}
