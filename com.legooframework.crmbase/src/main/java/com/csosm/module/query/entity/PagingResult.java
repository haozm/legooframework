package com.csosm.module.query.entity;

import com.csosm.commons.jdbc.sqlcfg.ColumnMeta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Smart <br>
 * 分页结果封装
 */
public class PagingResult {

    private final String model, stmtId;
    private final long count;
    private final int pageNum, pageSize;
    private List<Map<String, Object>> resultSet;

    public PagingResult(String model, String stmtId, long count, int pageNum, int pageSize,
                        List<Map<String, Object>> resultSet) {
        this.model = model;
        this.stmtId = stmtId;
        this.count = count;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.resultSet = resultSet;
    }

    public static PagingResult emptyPagingResult(String model, String stmtId) {
        return new PagingResult(model, stmtId, 0, 0, 20, null);
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

    public boolean countIsZore() {
        return count == 0;
    }

    public boolean isNotEmpty() {
        return !CollectionUtils.isEmpty(resultSet);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("total", count);
        map.put("data", CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        return map;
    }

    public Map<String, Object> toMap(List<ColumnMeta> metas) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("total", count);
        map.put("metas", metas);
        map.put("data", CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        return map;
    }

    public static Map<String, Object> toEmpty() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("total", 0);
        map.put("data", null);
        return map;
    }

    public Optional<List<Map<String, Object>>> getResultSet() {
        return Optional.fromNullable(CollectionUtils.isEmpty(resultSet) ? null : resultSet);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("model", model)
                .add("stmtId", stmtId)
                .add("total", count)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .add("resultSet's size ", CollectionUtils.isEmpty(resultSet) ? 0 : resultSet.size())
                .toString();
    }
}
