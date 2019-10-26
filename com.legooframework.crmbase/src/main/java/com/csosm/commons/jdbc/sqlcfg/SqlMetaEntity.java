package com.csosm.commons.jdbc.sqlcfg;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlMetaEntity extends BaseEntity<String> {
    private static final Logger logger = LoggerFactory.getLogger(SqlMetaEntity.class);
    private final String modelName, stmtId;
    private final String[] macros;
    private String sql;
    private String desc;
    private final boolean dynamic;
    private final List<ColumnMeta> columnMetas;
    private final List<QueryParam> queryParams;

    SqlMetaEntity(String modelName, String stmtId, String sql, String[] macros,
                  String desc, boolean dynamic, List<ColumnMeta> columnMetas,
                  List<QueryParam> queryParams) {
        super(String.format("%s.%s", modelName, stmtId));
        this.modelName = modelName;
        this.stmtId = stmtId;
        this.desc = desc;
        this.macros = macros;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(modelName));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sql));
        this.sql = sql;
        this.dynamic = dynamic;
        this.columnMetas = CollectionUtils.isEmpty(columnMetas) ? null : ImmutableList.copyOf(columnMetas);
        this.queryParams = CollectionUtils.isEmpty(queryParams) ? null : ImmutableList.copyOf(queryParams);

    }

    public Optional<List<QueryParam>> getQueryParams() {
        return Optional.fromNullable(CollectionUtils.isEmpty(queryParams) ? null : queryParams);
    }

    void handleParams(Map<String, Object> dataModel) {
        if (CollectionUtils.isEmpty(queryParams)) return;
        for (QueryParam $it : queryParams) $it.handleParams(dataModel);
    }

    public Optional<List<QueryParam>> getRequiredParam() {
        if (CollectionUtils.isEmpty(queryParams)) return Optional.absent();
        List<QueryParam> list = Lists.newArrayList();
        for (QueryParam $it : queryParams) {
            if ($it.isRequired()) list.add($it);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    public Optional<List<ColumnMeta>> getColumnMetas() {
        return Optional.fromNullable(CollectionUtils.isEmpty(columnMetas) ? null : columnMetas);
    }
    
    public Optional<List<ColumnMeta>> getColumnMetas(String type) {
    	List<ColumnMeta> result = Lists.newArrayListWithCapacity(this.columnMetas.size());
    	for(ColumnMeta item : this.columnMetas) 
    		if(null != item.get(type)) result.add(item);
        return Optional.fromNullable(CollectionUtils.isEmpty(result) ? null : result);
    }

    public Optional<String> getDesc() {
        return Optional.fromNullable(desc);
    }

    public String getModelName() {
        return modelName;
    }

    public boolean isDynamic() {
        return dynamic || hasMacros();
    }

    public String getStmtId() {
        return stmtId;
    }

    public String[] getMacros() {
        return macros;
    }

    public boolean hasMacros() {
        return ArrayUtils.isNotEmpty(macros);
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("model", modelName)
                .add("stmtId", stmtId)
                .add("dynamic", dynamic)
                .add("sql", sql)
                .add("columnMetas", columnMetas)
                .add("queryParams", queryParams)
                .add("desc", desc)
                .toString();
    }
}
