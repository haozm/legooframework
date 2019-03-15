package com.csosm.commons.jdbc.sqlcfg;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SqlMetaEntityBuilder {

    private String modelName, stmtId, desc;
    private String sql;
    private String[] macros;
    private boolean dynamic;
    private List<ColumnMeta> columnMetas;
    private List<QueryParam> queryParams;

    public SqlMetaEntityBuilder(String modelName, String stmtId, boolean dynamic, String desc, String macros) {
        setModelName(modelName);
        setStmtId(stmtId);
        this.desc = desc;
        this.macros = Strings.isNullOrEmpty(macros) ? null : StringUtils.split(macros, ',');
        this.dynamic = dynamic;
        this.columnMetas = Lists.newArrayList();
        this.queryParams = Lists.newArrayList();
    }

    private void setModelName(String modelName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(modelName));
        this.modelName = modelName;
    }

    public void addQueryParam(QueryParam param) {
        this.queryParams.add(param);
    }

    public void addColumnMeta(ColumnMeta columnMeta) {
        this.columnMetas.add(columnMeta);
    }

    private void setStmtId(String stmtId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId));
        this.stmtId = stmtId;
    }

    public void setSql(String sql) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sql));
        this.sql = sql;
    }

    public SqlMetaEntity build() {
        return new SqlMetaEntity(modelName, stmtId, sql, macros, desc, dynamic, columnMetas, queryParams);
    }
}
