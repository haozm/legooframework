package com.csosm.commons.jdbc.sqlcfg;

import com.csosm.commons.server.FileModifiedReload;
import com.csosm.commons.vfs.MonitorFileSystem;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Smart on 2017/3/31. 可执行SQL的工厂类
 *
 * @author hxj
 */
public class SqlMetaEntityFactory implements FileModifiedReload {

    private static final Logger logger = LoggerFactory.getLogger(SqlMetaEntityFactory.class);
    private final Configuration configuration;
    private final HashBasedTable<String, String, SqlMetaEntity> sqlMetaEntityTable;
    private final Cache<String, Template> cache;
    private MonitorFileSystem monitorFileSystem;
    private static final String DEF_CACHE_POLICY = "initialCapacity=256,maximumSize=2048,expireAfterAccess=60m";
    private final Map<String, ColumnMeta> columnMetaMap;
    private final AbstractRulesModule rulesModule;
    private final Collection<File> sqlXmlCfgs;
    private final Map<String, String> macroMap;
    private final Pattern pattern;
    private final String endsWith;

    SqlMetaEntityFactory(Configuration configuration, AbstractRulesModule rulesModule, String endsWith,
                         MonitorFileSystem monitorFileSystem) {
        this.sqlMetaEntityTable = HashBasedTable.create();
        this.configuration = configuration;
        this.cache = CacheBuilder.from(DEF_CACHE_POLICY).build();
        this.columnMetaMap = Maps.newHashMap();
        this.rulesModule = rulesModule;
        this.pattern = Pattern.compile(endsWith);
        this.endsWith = endsWith;
        this.macroMap = Maps.newHashMap();
        this.monitorFileSystem = monitorFileSystem;
        this.sqlXmlCfgs = Lists.newArrayList();
    }

    private void resetColumnMetaMap(List<ColumnMeta> columnMetas) {
        this.columnMetaMap.clear();
        if (!CollectionUtils.isEmpty(columnMetas))
            for (ColumnMeta $it : columnMetas) columnMetaMap.put($it.getId(), $it);
    }

    public SqlMetaEntity getSqlMetaEntity(String model, String stmtId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(model), "model=%s 值非法", model);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "stmtId=%s 值非法", stmtId);
        Preconditions.checkState(sqlMetaEntityTable.contains(model, stmtId), "不存在[%s.%s]对应的SQL语句.", model, stmtId);
        return sqlMetaEntityTable.get(model, stmtId);
    }

    @Override
    public boolean isSupportFile(File fileName) {
        boolean res = pattern.matcher(fileName.getAbsolutePath()).matches();
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s match %s  res is %s", fileName.getAbsolutePath(), endsWith, res));
        return res;
    }

    public String getExecSql(String model, String stmtId, Map<String, Object> dataModel) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(model), "model 为空，无法获取SQL");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "stmtId 为空，无法获取SQL");
        Preconditions.checkState(sqlMetaEntityTable.contains(model, stmtId), "不存在[%s.%s]对应的SQL语句.", model, stmtId);
        SqlMetaEntity entity = sqlMetaEntityTable.get(model, stmtId);

        boolean handle_params = MapUtils.getBoolean(dataModel, "handle_params", false);

        if (StringUtils.endsWith(stmtId, "_count")) {
            SqlMetaEntity entity_all = sqlMetaEntityTable.get(model, StringUtils.substringBefore(stmtId, "_count"));
            if (entity_all.getQueryParams().isPresent() && !handle_params) {
                entity_all.handleParams(dataModel);
                dataModel.put("handle_params", true);
            }
        } else {
            if (entity.getQueryParams().isPresent() && !handle_params) {
                entity.handleParams(dataModel);
            }
        }

        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s.%s] -> %s", model, stmtId, entity.getSql()));
        if (entity.isDynamic()) {
            Template ftl_template = getFtlTemplate(model, stmtId);
            StringWriter sw = new StringWriter();
            try {
                ftl_template.process(dataModel, sw);
                String execSql = sw.toString();
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("[%s.%s] -> %s", model, stmtId, execSql));
                }
                return execSql;
            } catch (Exception e) {
                String err_msg = String.format("通过入参[%s.%s]解析获取Sql语句发生异常.....", model, stmtId);
                logger.error(err_msg, e);
                throw new RuntimeException(err_msg);
            }
        }
        return entity.getSql();
    }

    public Map<String, ColumnMeta> getColumnMetaMap() {
        return columnMetaMap;
    }

    private Template getFtlTemplate(String model, String stmtId) {
        Preconditions.checkState(
                sqlMetaEntityTable.contains(model, stmtId), "不存在[%s,%s]对应的SQL语句.", model, stmtId);
        final String cache_key = String.format("%s.%s", model, stmtId);
        Template sql_template = this.cache.getIfPresent(cache_key);
        if (sql_template == null) {
            synchronized (this.cache) {
                sql_template = this.cache.getIfPresent(cache_key);
                if (sql_template == null) {
                    SqlMetaEntity metaEntity = sqlMetaEntityTable.get(model, stmtId);
                    String sql_src;
                    if (metaEntity.hasMacros()) {
                        StringBuilder sb = new StringBuilder();
                        for (String $it : metaEntity.getMacros())
                            sb.append(this.macroMap.get($it));
                        sb.append(metaEntity.getSql());
                        sql_src = sb.toString();
                    } else {
                        sql_src = sqlMetaEntityTable.get(model, stmtId).getSql();
                    }
                    try {
                        sql_template = new Template(cache_key, new StringReader(sql_src), this.configuration);
                    } catch (IOException e) {
                        logger.error(String.format("构建 %s :ftl_template发生异常....", cache_key), e);
                        throw new RuntimeException(e);
                    }
                    this.cache.put(cache_key, sql_template);
                }
            }
        } else {
            if (logger.isDebugEnabled())
                logger.debug(
                        String.format("load Optional<Template> from Cache which Id is %s.%s", model, stmtId));
        }
        return sql_template;
    }

    /**
     * 鸡蛋要从内部打破 祸起萧墙
     */
    @Override
    public void building(Collection<File> empty) {
        boolean error = false;
        Optional<List<File>> files = monitorFileSystem.findFiles(endsWith);
        if (files.isPresent()) {
            Table<String, String, SqlMetaEntity> sqlMetaEntityTable = HashBasedTable.create();
            List<ColumnMeta> columnMetas = Lists.newArrayList();
            Map<String, String> macroMap = Maps.newHashMap();
            Digester digester = DigesterLoader.newLoader(rulesModule).newDigester();
            for (File $it : files.get()) {
                digester.push(sqlMetaEntityTable);
                digester.push("ColumnMeta", columnMetas);
                digester.push("macroMap", macroMap);
                try {
                    digester.parse($it);
                    if (logger.isDebugEnabled()) logger.debug(String.format("finish parse sql-cfg: %s", $it));
                } catch (Exception e) {
                    logger.error(String.format("parse file=%s has error", $it), e);
                    error = true;
                } finally {
                    digester.clear();
                }
                if (error)
                    break;
            }

            if (!error) {
                this.sqlXmlCfgs.clear();
                this.sqlXmlCfgs.addAll(files.get());
                resetColumnMetaMap(columnMetas);
                this.sqlMetaEntityTable.clear();
                this.sqlMetaEntityTable.putAll(sqlMetaEntityTable);
                this.macroMap.clear();
                this.macroMap.putAll(macroMap);
                this.cache.invalidateAll();
            }
        }
    }
}
