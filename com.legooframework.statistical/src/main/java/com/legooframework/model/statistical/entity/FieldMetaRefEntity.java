package com.legooframework.model.statistical.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.util.Map;
import java.util.Optional;

public class FieldMetaRefEntity extends BaseEntity<String> {

    private final String serieType, axisY, group;
    private final boolean drill, primary, order;

    public FieldMetaRefEntity(String id, boolean drill, boolean order, boolean primary, String serieType, String axisY,
                              String group) {
        super(id);
        this.serieType = serieType;
        this.drill = drill;
        this.axisY = axisY;
        this.primary = primary;
        this.order = order;
        this.group = group;
    }

    Optional<String> getGroup() {
        return Optional.ofNullable(group);
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("drill", drill);
        params.put("primary", primary);
        params.put("id", getId());
        params.put("group", group);
        params.put("order", order);
        params.put("serieType", Strings.nullToEmpty(serieType));
        params.put("axisY", Strings.nullToEmpty(axisY));
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("refId", getId())
                .add("drill", drill)
                .add("order", order)
                .add("group", group)
                .add("serieType", serieType)
                .add("axisY", axisY)
                .omitNullValues()
                .toString();
    }
}
