package com.legooframework.model.statistical.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TableMetaEntity extends BaseEntity<String> {

    private final String title, type, sql;
    private final String linkUrl;
    private final List<FieldMetaRefEntity> fieldMetaRefs;

    public TableMetaEntity(String id, String title, String sql, String linkUrl, List<FieldMetaRefEntity> fieldMetaRefs) {
        super(id);
        this.title = title;
        this.type = "table";
        this.linkUrl = linkUrl;
        this.sql = sql;
        this.fieldMetaRefs = ImmutableList.copyOf(fieldMetaRefs);
    }


    Optional<String> getLinkUrl() {
        return Optional.ofNullable(linkUrl);
    }

    String getSql() {
        return sql;
    }

    Map<String, Object> toViewMap(StatisticalEntity statistical) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("type", type);
        params.put("rid", statistical.getId());
        params.put("id", this.getId());
        params.put("title", title);
        params.put("sql", sql);
        if (null != linkUrl) params.put("linkUrl", linkUrl);
        List<Map<String, Object>> fields = Lists.newArrayList();
        for (FieldMetaRefEntity $it : fieldMetaRefs) {
            FieldMetaEntity fieldMeta = statistical.loadFieldById($it.getId());
            Map<String, Object> field = fieldMeta.toViewMap();
            field.putAll($it.toViewMap());
            fields.add(field);
        }
        params.put("fields", fields);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("title", title)
                .add("sql", sql)
                .add("linkUrl", linkUrl)
                .add("fieldMetaRefs", fieldMetaRefs)
                .toString();
    }
}
