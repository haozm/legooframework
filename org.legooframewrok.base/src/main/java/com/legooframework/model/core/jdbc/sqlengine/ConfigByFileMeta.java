package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;
import java.util.Optional;

class ConfigByFileMeta {

    private final File file;
    private final Map<String, String> macros;
    private final Table<String, String, SQLStatement> statements;

    ConfigByFileMeta(File file, Map<String, String> macros, Table<String, String, SQLStatement> statements) {
        Preconditions.checkNotNull(file, "配置文件不可以为空.");
        this.file = file;
        this.macros = MapUtils.isEmpty(macros) ? null : Maps.newHashMap(macros);
        this.statements = statements;
    }

    public File getFile() {
        return file;
    }

    boolean containsMac(String macroIds) {
        return this.macros.containsKey(macroIds);
    }

    Optional<String> getMacro(String macroId) {
        if (MapUtils.isEmpty(macros)) return Optional.empty();
        return Optional.ofNullable(this.macros.get(macroId));
    }

    Optional<SQLStatement> getStmtById(String model, String stmtId) {
        return Optional.ofNullable(statements.get(model, stmtId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigByFileMeta)) return false;
        ConfigByFileMeta that = (ConfigByFileMeta) o;
        return StringUtils.equals(file.getAbsolutePath(), that.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file.getAbsolutePath());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("file", file.getAbsolutePath())
                .add("macros", macros == null ? "0" : macros.size())
                .add("statements", statements.size())
                .toString();
    }
}
