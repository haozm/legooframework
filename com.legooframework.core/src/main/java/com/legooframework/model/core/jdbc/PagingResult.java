package com.legooframework.model.core.jdbc;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PagingResult {

    private final String model, stmtId;
    private final long count;
    private final int pageNum, pageSize;
    private List<Map<String, Object>> resultSet;

    public PagingResult(String model, String stmtId, long count,
                        int pageNum, int pageSize, List<Map<String, Object>> resultSet) {
        this.model = model;
        this.stmtId = stmtId;
        this.count = count;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.resultSet = resultSet;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getModel() {
        return model;
    }

    public String getStmtId() {
        return stmtId;
    }

    public long getCount() {
        return count;
    }

    public Map<String, Object> toData() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("total", count);
        map.put("data", CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        return map;
    }

    public Optional<List<Map<String, Object>>> getResultSet() {
        return CollectionUtils.isEmpty(resultSet) ? Optional.empty() : Optional.of(resultSet);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("model", model)
                .add("stmtId", stmtId)
                .add("count", count)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .add("resultSet's size ", CollectionUtils.isEmpty(resultSet) ? 0 : resultSet.size())
                .toString();
    }
}
