package com.csosm.module.webocx.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class CdnItem {
    private final String name, value;
    private final boolean expression;

    CdnItem(String name, String value) {
        this.name = name;
        this.expression = StringUtils.startsWith(value, ":");
        this.value = StringUtils.removeStart(value, ":");
    }

    public void holdParam(Map<String, Object> params) {
        if (expression) {
            Object _val = params.get(value);
            params.put(name, _val);
        } else {
            params.put(name, value);
        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CdnItem)) return false;
        CdnItem cdnItem = (CdnItem) o;
        return expression == cdnItem.expression &&
                Objects.equal(name, cdnItem.name) &&
                Objects.equal(value, cdnItem.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, value, expression);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("value", value)
                .add("expression", expression)
                .toString();
    }
}
