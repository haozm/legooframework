package com.csosm.module.webocx.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class SubPageDefined {

    private final boolean paged;
    private final String id, name, stmtId, url;
    private final String[] keys;
    private final String[] sqlId;

    SubPageDefined(boolean paged, String id, String name, String stmtId, String url, String[] keys) {
        this.paged = paged;
        this.id = id;
        this.name = name;
        this.stmtId = stmtId;
        this.url = url;
        this.keys = keys;
        this.sqlId = StringUtils.split(stmtId, '.');
    }

    public String getSqlModel() {
        return sqlId[0];
    }

    public String getSqlStmtId() {
        return sqlId[1];
    }

    public boolean isPaged() {
        return paged;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStmtId() {
        return stmtId;
    }

    public String getUrl() {
        return url;
    }

    public String[] getKeys() {
        return keys;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("name", id);
        param.put("title", name);
        param.put("page", paged);
        param.put("view", "grid");
        param.put("querykey", getKeys());
        param.put("url", url);
        return param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubPageDefined that = (SubPageDefined) o;
        return paged == that.paged &&
                Objects.equal(id, that.id) &&
                Objects.equal(name, that.name) &&
                Objects.equal(stmtId, that.stmtId) &&
                Objects.equal(url, that.url) &&
                Objects.equal(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(paged, id, name, stmtId, url, keys);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("paged", paged)
                .add("id", id)
                .add("name", name)
                .add("stmtId", stmtId)
                .add("url", url)
                .add("keys", keys)
                .toString();
    }
}
