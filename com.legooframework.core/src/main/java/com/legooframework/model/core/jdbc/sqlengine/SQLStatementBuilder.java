package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SQLStatementBuilder {
    private String model, stmtId;
    private String[] macros;
    private List<SubStatement> statements;
    private String desc;
    private boolean dynamic;
    private List<ColumnMeta> columns;
    private List<ParamMeta> params;
    private String[] includs;
    private String[] excluds;

    public SQLStatementBuilder(String model, String stmtId, String[] macros, String desc, boolean dynamic) {
        this.model = model;
        this.stmtId = stmtId;
        this.macros = macros;
        this.statements = Lists.newArrayList();
        this.desc = desc;
        this.columns = Lists.newArrayList();
        this.params = Lists.newArrayList();
        this.dynamic = dynamic;
    }

    public void addColumn(ColumnMeta col) {
        this.columns.add(col);
    }

    public void addParam(ParamMeta param) {
        this.params.add(param);
    }

    public void setStatement(SubStatement statement) {
        Preconditions.checkNotNull(statement);
        this.statements.add(statement);
    }

    public void setIncluds(String[] includs) {
        if (ArrayUtils.isNotEmpty(includs))
            this.includs = ArrayUtils.clone(includs);
    }

    public void setExcluds(String[] excluds) {
        if (ArrayUtils.isNotEmpty(excluds))
            this.excluds = ArrayUtils.clone(excluds);
    }

    public SQLStatement build() {
        if (this.statements.size() == 1) {
            return new SQLStatement(model, stmtId, macros, statements.get(0), desc, dynamic, columns, params,
                    includs, excluds);
        } else {
            return new SQLStatement(model, stmtId, macros, statements, desc, dynamic, columns, params, includs, excluds);
        }

    }
}
