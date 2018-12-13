package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLStatement {
    private final String model, stmtId;
    private final String[] macros;
    private final List<SubStatement> statements;
    private final boolean single;
    private final String desc;
    private final boolean dynamic;
    private final List<ColumnMeta> columns;
    private final List<ParamMeta> params;
    private final RolesSupport rolesSupport;

    SQLStatement(String model, String stmtId, String[] macros, SubStatement statement,
                 String desc, boolean dynamic, List<ColumnMeta> columns, List<ParamMeta> params,
                 String[] includs, String[] excluds) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(model));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId));
        Preconditions.checkNotNull(statement);
        this.model = model;
        this.stmtId = stmtId;
        this.macros = macros;
        this.statements = Lists.newArrayListWithCapacity(1);
        this.statements.add(statement);
        this.single = true;
        this.desc = desc;
        this.dynamic = dynamic;
        this.columns = CollectionUtils.isEmpty(columns) ? null : ImmutableList.copyOf(columns);
        this.params = CollectionUtils.isEmpty(params) ? null : ImmutableList.copyOf(params);
        this.rolesSupport = new RolesSupport(includs, excluds);
    }

    SQLStatement(String model, String stmtId, String[] macros, List<SubStatement> statements,
                 String desc, boolean dynamic, List<ColumnMeta> columns, List<ParamMeta> params,
                 String[] includs, String[] excluds) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(model));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId));
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(statements) && statements.size() > 1);
        this.model = model;
        this.stmtId = stmtId;
        this.macros = macros;
        this.statements = ImmutableList.copyOf(statements);
        this.single = false;
        this.desc = desc;
        this.dynamic = dynamic;
        this.columns = CollectionUtils.isEmpty(columns) ? null : ImmutableList.copyOf(columns);
        this.params = CollectionUtils.isEmpty(params) ? null : ImmutableList.copyOf(params);
        this.rolesSupport = new RolesSupport(includs, excluds);
    }

    public String getModel() {
        return model;
    }

    public Optional<List<ColumnMeta>> getColumns() {
        return Optional.ofNullable(this.columns);
    }

    public boolean isSingle() {
        return single;
    }

    // 针对配置的参数 格式化处理
    public void handleParams(Map<String, Object> queryParams) {
        if (CollectionUtils.isEmpty(params)) return;
        for (ParamMeta $p : params) $p.handleParams(queryParams);
    }

    public String getStmtId() {
        return stmtId;
    }

    public String getStatement() {
        Preconditions.checkState(isSingle(), "该查询为多语句查询，不支持该方法调用.");
        return statements.get(0).getStatement();
    }

    public List<SubStatement> getStatements() {
        Preconditions.checkState(!isSingle(), "该查询单语句查询，不支持该方法调用.");
        return statements;
    }

    public boolean needFmt() {
        return dynamic || ArrayUtils.isNotEmpty(macros);
    }

    public boolean exitsMacros() {
        return ArrayUtils.isNotEmpty(macros);
    }

    public String[] getMacros() {
        return macros;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLStatement that = (SQLStatement) o;
        return dynamic == that.dynamic &&
                Objects.equal(model, that.model) &&
                Objects.equal(stmtId, that.stmtId) &&
                Objects.equal(macros, that.macros) &&
                Objects.equal(statements, that.statements) &&
                Objects.equal(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model, stmtId, macros, statements, desc, dynamic);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("model", model)
                .add("stmtId", stmtId)
                .add("macros", Arrays.toString(macros))
                .add("statements", statements)
                .add("desc", desc)
                .add("dynamic", dynamic)
                .add("columns", columns == null ? 0 : columns.size())
                .add("params", params == null ? 0 : params.size())
                .add("params", params == null ? 0 : params.size())
                .add("RolesSupport", rolesSupport.toString())
                .toString();
    }

    class RolesSupport {
        private final String[] includs;
        private final String[] excluds;

        public RolesSupport(String[] includs, String[] excluds) {
            this.includs = includs;
            this.excluds = excluds;
        }

        boolean isEmpty() {
            return ArrayUtils.isEmpty(includs) && ArrayUtils.isEmpty(excluds);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("includs=").append(Arrays.toString(includs));
            sb.append(", excluds=").append(Arrays.toString(excluds));
            return sb.toString();
        }
    }
}
