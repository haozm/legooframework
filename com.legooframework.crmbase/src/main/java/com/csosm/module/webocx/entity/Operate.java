package com.csosm.module.webocx.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;

public class Operate {

    private final String name, title, type, url;
    private final String[] keys;
    private final Map<String, Object> map;

    Operate(String name, String title, String type, String url, String[] keys) {
        this.name = name;
        this.title = title;
        this.type = type;
        this.url = url;
        this.keys = keys;
        Map<String, Object> mm = Maps.newHashMap();
        mm.put("name", name);
        mm.put("title", title);
        if (!Strings.isNullOrEmpty(this.url)) mm.put("url", url);
        mm.put("type", type);
        if (!ArrayUtils.isEmpty(this.keys)) mm.put("querykey", keys);
        this.map = ImmutableMap.copyOf(mm);
    }

    public Map<String, Object> toMap() {
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operate operate = (Operate) o;
        return Objects.equal(name, operate.name) &&
                Objects.equal(title, operate.title) &&
                Objects.equal(type, operate.type) &&
                Objects.equal(url, operate.url) &&
                Objects.equal(keys, operate.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, title, type, url, keys);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("title", title)
                .add("type", type)
                .add("url", url)
                .add("keys", keys)
                .toString();
    }
}
