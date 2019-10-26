package com.legooframework.model.core.jdbc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AsyncResult {
    private final String model, stmtId;
    private final int type;
    private Map<String, Object> params, map;
    private List<Map<String, Object>> listMap;

    private AsyncResult(String model, String stmtId, int type, Map<String, Object> params, Map<String, Object> map, List<Map<String, Object>> listMap) {
        this.model = model;
        this.stmtId = stmtId;
        this.type = type;
        this.params = Maps.newHashMap(params);
        this.map = map;
        this.listMap = listMap;
    }

    Object getParams(String key) {
        return params.get(key);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    static AsyncResult creat4Map(String model, String stmtId, Map<String, Object> params, Map<String, Object> map) {
        return new AsyncResult(model, stmtId, 0, params, map, null);
    }

    static AsyncResult creat4ListMap(String model, String stmtId, Map<String, Object> params, List<Map<String, Object>> listMap) {
        return new AsyncResult(model, stmtId, 1, params, null, listMap);
    }

    public Optional<Map<String, Object>> getMapIfExits() {
        Preconditions.checkState(type == 0);
        return Optional.ofNullable(MapUtils.isEmpty(map) ? null : map);
    }

    public Optional<List<Map<String, Object>>> getListMapIfExits() {
        Preconditions.checkState(type == 1);
        return Optional.ofNullable(CollectionUtils.isEmpty(listMap) ? null : listMap);
    }

    public int getSize() {
        if (type == 0) {
            return MapUtils.isEmpty(map) ? 0 : map.size();
        } else {
            return CollectionUtils.isEmpty(listMap) ? 0 : listMap.size();
        }
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this)
                .add("model", model)
                .add("stmtId", stmtId).add("params", params);
        if (type == 0) {
            helper.add("map", map);
        } else {
            helper.add("listMap", listMap);
        }
        return helper.toString();
    }
}
