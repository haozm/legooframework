package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.legooframework.model.core.config.FileReloadSupport;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLStatementFactory extends FileReloadSupport<ConfigByFileMeta> {

    private static final Logger logger = LoggerFactory.getLogger(SQLStatementFactory.class);
    private static final String DEF_CACHE_POLICY = "initialCapacity=64,maximumSize=1024,expireAfterAccess=60m";
    private final Cache<String, Template> cache;
    private final Configuration configuration;
    private final RulesModule rulesModule;

    SQLStatementFactory(Configuration configuration, RulesModule rulesModule, List<String> patterns) {
        super(patterns);
        this.configuration = configuration;
        this.rulesModule = rulesModule;
        this.cache = CacheBuilder.from(DEF_CACHE_POLICY).build();
    }

    Optional<SQLStatement> findStmtById(final String model, final String stmtId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(model), "String model can not be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "String stmtId can not be null");
        Optional<SQLStatement> optional;
        if (getConfigs().isPresent())
            for (ConfigByFileMeta $it : getConfigs().get()) {
                optional = $it.getStmtById(model, stmtId);
                if (optional.isPresent()) return optional;
            }
        return Optional.empty();
    }

    public SQLStatement loadStmtById(String model, String stmtId) {
        Optional<SQLStatement> optional = findStmtById(model, stmtId);
        Preconditions.checkState(optional.isPresent(),
                "不存在model =%s,stmtId=%s 对应的SQL定义.", model, stmtId);
        return optional.get();
    }

    public boolean contains(String model, String stmtId) {
        Optional<SQLStatement> optional = findStmtById(model, stmtId);
        return optional.isPresent();
    }

    Optional<String> getMacro(String macro_id) {
        Optional<String> optional;
        if (getConfigs().isPresent())
            for (ConfigByFileMeta $it : getConfigs().get()) {
                optional = $it.getMacro(macro_id);
                if (optional.isPresent()) return optional;
            }
        return Optional.empty();
    }

    // 获取可执行的SQL语句
    public String getExecSql(String model, String stmtId, Map<String, Object> params) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(model), "String model can not be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "String stmtId can not be null");
        Preconditions.checkState(this.contains(model, stmtId),
                "不存在model =%s,stmtId=%s 对应的SQL定义.", model, stmtId);

        SQLStatement statement = loadStmtById(model, stmtId);
        // 验证入参以及格式化输出参数
        statement.handleParams(params);

        if (logger.isTraceEnabled())
            logger.trace(String.format("[%s.%s] -> %s", model, stmtId, statement.getStatement()));
        List<String> macros = null;
        if (statement.exitsMacros()) {
            macros = Lists.newArrayList();
            Optional<String> optional;
            for (String $it : statement.getMacros()) {
                optional = getMacro($it);
                Preconditions.checkState(optional.isPresent(), "不存在 %s 对应的宏定义.", $it);
                macros.add(optional.get());
            }
        }

        if (statement.needFmt()) {
            Template ftl_template = getFtlTemplate(statement, macros);
            StringWriter sw = new StringWriter();
            try {
                ftl_template.process(params, sw);
                String execSql = sw.toString();
                if (logger.isTraceEnabled())
                    logger.trace(String.format("[%s.%s] -> %s", model, stmtId, execSql));
                return execSql;
            } catch (Exception e) {
                String err_msg = String.format("通过入参[%s.%s]解析获取Sql语句发生异常.....", model, stmtId);
                logger.error(err_msg, e);
                throw new FormatSqlException(err_msg, e);
            }
        }
        return statement.getStatement();
    }

    // Cache freemarker and return Template
    private Template getFtlTemplate(SQLStatement statement, List<String> macros) {
        final String cache_key = String.format("%s_cache_%s", statement.getModel(), statement.getStmtId());
        Template sql_template = this.cache.getIfPresent(cache_key);
        if (sql_template == null) {
            synchronized (this.cache) {
                sql_template = this.cache.getIfPresent(cache_key);
                if (sql_template == null) {
                    String sql_src;
                    if (statement.exitsMacros()) {
                        StringBuilder sb = new StringBuilder();
                        if (!CollectionUtils.isEmpty(macros))
                            macros.forEach(sb::append);
                        sb.append(statement.getStatement());
                        sql_src = sb.toString();
                    } else {
                        sql_src = statement.getStatement();
                    }
                    try {
                        sql_template = new Template(cache_key, new StringReader(sql_src), this.configuration);
                    } catch (IOException e) {
                        logger.error(String.format("构建 %s :ftl_template 发生异常....", cache_key), e);
                        throw new RuntimeException(e);
                    }
                    this.cache.put(cache_key, sql_template);
                }
            }
        } else {
            if (logger.isDebugEnabled())
                logger.debug(String.format("load Optional<Template> from Cache which Id is %s.%s",
                        statement.getModel(), statement.getStmtId()));
        }
        return sql_template;
    }

    @Override
    public void addConfig(File file, ConfigByFileMeta config) {
        super.addConfig(file, config);
    }

    @Override
    protected void updateConfig(File file, ConfigByFileMeta config) {
        super.updateConfig(file, config);
        this.cache.invalidateAll();
    }

    @Override
    protected Optional<ConfigByFileMeta> parseFile(File file) {
        if (!isSupported(file)) return Optional.empty();
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        Table<String, String, SQLStatement> statements = HashBasedTable.create();
        Map<String, String> macros = Maps.newHashMap();
        try {
            digester.push("macros", macros);
            digester.push("statements", statements);
            digester.parse(file);
            ConfigByFileMeta fileMeta = new ConfigByFileMeta(file, macros, statements);
            if (logger.isDebugEnabled()) logger.debug(String.format("finish parse sql-cfg: %s", fileMeta));
            return Optional.of(fileMeta);
        } catch (Exception e) {
            logger.error(String.format("parse file=%s has error", file), e);
        } finally {
            digester.clear();
        }
        return Optional.empty();
    }

}
