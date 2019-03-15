package com.csosm.module.webocx.entity;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class DataSource {

    private Type type;
    private final String context;
    private final List<LabelName> labelNames;

    DataSource(String type, String context, List<String> labelNames) {
        setType(type);
        this.context = context;
        if (CollectionUtils.isEmpty(labelNames)) {
            this.labelNames = null;
        } else {
            List<LabelName> list = Lists.newArrayList();
            for (String $it : labelNames) {
                String[] res = StringUtils.split($it, ':');
                list.add(new LabelName(res[0], res[1], "true".equals(res[2])));
            }
            this.labelNames = Lists.newArrayList(list);
        }
    }

    void setType(String type) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type));
        Optional<Type> type_opt = Enums.getIfPresent(DataSource.Type.class, type);
        Preconditions.checkState(type_opt.isPresent(), "%s 值非法，无法转换为合法的 DataSource.Type 类型...", type);
        this.type = type_opt.get();
    }

    public List<Map<String, Object>> toViewMap() {
        Preconditions.checkState(isEnum());
        List<Map<String, Object>> list = Lists.newArrayList();
        for (LabelName $it : labelNames) {
            list.add($it.toViewMap());
        }
        return list;
    }

    public boolean isDict() {
        return Type.DICT == this.type;
    }

    public boolean isQuery() {
        return Type.QUERY == this.type;
    }

    public boolean isNotEmpty() {
        return CollectionUtils.isEmpty(labelNames);
    }

    public void setLabelName(List<LabelName> list) {
        this.labelNames.addAll(list);
    }

    public List<LabelName> getLabelNames() {
        Preconditions.checkState(isNotEmpty(), "非法请求...");
        return labelNames;
    }

    public boolean isEnum() {
        return Type.ENUM == this.type;
    }

    public boolean isSql() {
        return Type.SQL == this.type;
    }

    public Type getType() {
        return type;
    }

    public String getContext() {
        return context;
    }

    enum Type {
        DICT, SQL, ENUM, QUERY
    }

    class LabelName {
        private String label, value;
        private boolean checked;

        LabelName(String label, String value, boolean checked) {
            this.label = label;
            this.value = value;
            this.checked = checked;
        }

        Map<String, Object> toViewMap() {
            Map<String, Object> data = Maps.newHashMap();
            data.put("label", label);
            data.put("value", value);
            if (checked) data.put("checked", true);
            return data;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("label", label)
                    .add("value", value)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("context", context)
                .toString();
    }
}
