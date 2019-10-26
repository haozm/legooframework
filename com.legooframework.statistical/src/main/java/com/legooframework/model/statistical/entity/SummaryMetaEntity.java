package com.legooframework.model.statistical.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

public class SummaryMetaEntity extends BaseEntity<String> {

    private final String title, sql, subpage;
    private final List<FieldMetaRefEntity> fieldMetaRefs;

    public SummaryMetaEntity(String id, String title, String sql, String subpage, List<FieldMetaRefEntity> fieldMetaRefs) {
        super(id);
        this.title = title;
        this.sql = sql;
        this.subpage = subpage;
        this.fieldMetaRefs = CollectionUtils.isEmpty(fieldMetaRefs) ? null : ImmutableList.copyOf(fieldMetaRefs);
    }

    Map<String, Object> toViewMap(StatisticalEntity statistical) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("title", title);
        params.put("type", "summary");
        params.put("sql", sql);
        if (Strings.isNullOrEmpty(subpage)) {
            params.put("rid", statistical.getId());
        } else {
            params.put("rid", subpage);
        }
        params.put("id", String.format("%s.%s", statistical.getId(), this.getId()));
        params.put("group", false);
        List<Object> fields = Lists.newArrayList();
        Multimap<String, Map<String, Object>> multimap = ArrayListMultimap.create();
        for (FieldMetaRefEntity $it : fieldMetaRefs) {
            if ($it.getGroup().isPresent()) {
                Map<String, Object> field = $it.toViewMap();
                FieldMetaEntity fieldMeta = statistical.loadFieldById($it.getId());
                field.putAll(fieldMeta.toViewMap());
                multimap.put($it.getGroup().get(), field);
            } else {
                Map<String, Object> field = $it.toViewMap();
                FieldMetaEntity fieldMeta = statistical.loadFieldById($it.getId());
                field.putAll(fieldMeta.toViewMap());
                fields.add(field);
            }
        }
        if (!multimap.isEmpty()) {
            for (String $it : multimap.keySet()) fields.add(multimap.get($it));
            params.put("group", true);
        }
        params.put("fields", fields);
        return params;
    }

    String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("title", title)
                .add("subpage", subpage)
                .add("sql", sql)
                .add("fieldMetaRefs", fieldMetaRefs)
                .toString();
    }


}
