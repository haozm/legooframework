package com.legooframework.model.statistical.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.util.List;
import java.util.Map;

public class EchartMetaEntity extends BaseEntity<String> {

    private final String type, title, axisY1Title, axisY2Title, sql;
    private final List<FieldMetaRefEntity> fieldMetaRefs;

    public EchartMetaEntity(String id, String type, String title, String sql, String axisY1Title, String axisY2Title,
                            List<FieldMetaRefEntity> fieldMetaRefs) {
        super(id);
        this.type = type;
        this.title = title;
        this.sql = sql;
        this.axisY1Title = axisY1Title;
        this.axisY2Title = axisY2Title;
        this.fieldMetaRefs = ImmutableList.copyOf(fieldMetaRefs);
    }

    Map<String, Object> toViewMap(StatisticalEntity statistical) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", String.format("%s.%s", statistical.getId(), this.getId()));
        params.put("rid", statistical.getId());
        params.put("type", type);
        params.put("title", title);
        params.put("sql", sql);
        params.put("axisY1Title", Strings.nullToEmpty(axisY1Title));
        params.put("axisY2Title", Strings.nullToEmpty(axisY2Title));
        List<Map<String, Object>> fields = Lists.newArrayList();
        for (FieldMetaRefEntity $it : fieldMetaRefs) {
            Map<String, Object> field = $it.toViewMap();
            FieldMetaEntity fieldMeta = statistical.loadFieldById($it.getId());
            field.putAll(fieldMeta.toViewMap());
            fields.add(field);
        }
        params.put("fields", fields);
        return params;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("type", type)
                .add("axisY1Title", axisY1Title)
                .add("axisY2Title", axisY2Title)
                .add("fieldMetaRefs", fieldMetaRefs)
                .toString();
    }
}
