package com.legooframework.model.core.jdbc;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatement;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class JdbcQuerySupport extends NamedParameterJdbcDaoSupport {

    private static final Logger logger = LoggerFactory.getLogger(JdbcQuerySupport.class);

    public PagingResult queryForPage(String model, String stmtId, int pageNum, int pageSize, Map<String, Object> params) {
        int _size = pageSize <= 0 ? 20 : pageSize;
        long offset = pageNum <= 1 ? 0 : (pageNum - 1) * _size;
        checkParams(model, stmtId, params);
        Map<String, Object> _paramMap = MapUtils.isEmpty(params) ? Maps.newHashMap() : Maps.newHashMap(params);
        _paramMap.put("offset", offset);
        _paramMap.put("rows", _size);
        String execSql = statementFactory.getExecSql(model, String.format("%s_count", stmtId), _paramMap);
        if (logger.isDebugEnabled())
            logger.debug(String.format("\nsql-> [%s.%s] \n params-> [%s] \n execsql -> %s",
                    model, stmtId, params, execSql));
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        @SuppressWarnings("ConstantConditions")
        long count = getQueryTemplate().queryForObject(execSql, _paramMap, Long.class);
        stopwatch.stop();
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryForPage [%s,%s_count result is %s and elapsed %s ms]",
                    model, stmtId, count, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        if (count == 0 || offset > count)
            return new PagingResult(model, stmtId, count, pageNum, pageSize, null);
        Optional<List<Map<String, Object>>> optional = queryForList(model, stmtId, _paramMap);
        return new PagingResult(
                model, stmtId, count, pageNum, pageSize, optional.orElse(null));
    }

    public Optional<Map<String, Object>> queryForMap(String model, String stmtId, Map<String, Object> params) {
        checkParams(model, stmtId, params);
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(String.format("\nsql-> [%s.%s] \n params-> [%s] \n execsql -> %s",
                        model, stmtId, params, execSql));

            Map<String, Object> result = getQueryTemplate().queryForMap(execSql, params);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForMap [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, result, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.ofNullable(MapUtils.isEmpty(result) ? null : result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "queryForMap", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    public Optional<List<Map<String, Object>>> queryForList(String directSql, Map<String, Object> params) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            List<Map<String, Object>> resultSet = getQueryTemplate().queryForList(directSql, params);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForList [%sresult's size is %s and elapsed %s ms]",
                        directSql, CollectionUtils.isEmpty(resultSet) ? 0 : resultSet.size(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.ofNullable(CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        } catch (Exception e) {
            String err_msg = String.format("%s whit %s has error", "queryForList", directSql, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    public Optional<Map<String, Object>> queryForMap(String model, String stmtId, String key, Object value) {
        Map<String, Object> params = Maps.newHashMap();
        if (value != null) params.put(Strings.isNullOrEmpty(key) ? "value" : key, value);
        return this.queryForMap(model, stmtId, params);
    }

    public Optional<List<Map<String, Object>>> queryForList(String model, String stmtId, Map<String, Object> params) {
        checkParams(model, stmtId, params);
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (logger.isDebugEnabled())
            logger.debug(String.format("\nsql-> [%s.%s] \n params-> [%s] \n execsql -> %s",
                    model, stmtId, params, execSql));
        try {
            List<Map<String, Object>> resultSet = getQueryTemplate().queryForList(execSql, params);
            stopwatch.stop();

            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForList [%s,%s result's size is %s and elapsed %s ms]",
                        model, stmtId, CollectionUtils.isEmpty(resultSet) ? 0 : resultSet.size(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));

            return Optional.ofNullable(CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        } catch (Exception e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "queryForList", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    public Optional<List<Map<String, Object>>> queryForList(String model, String stmtId, String key, Object value) {
        Map<String, Object> params = Maps.newHashMap();
        if (value != null) params.put(Strings.isNullOrEmpty(key) ? "value" : key, value);
        return this.queryForList(model, stmtId, params);
    }

    public <T> Optional<List<T>> queryForList(String model, String stmtId, Map<String, Object> params, Class<T> clazz) {
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        List<T> reslist = getQueryTemplate().queryForList(execSql, params, clazz);
        return Optional.ofNullable(CollectionUtils.isEmpty(reslist) ? null : reslist);
    }

    public <T> Optional<T> queryForObject(String model, String stmtId, Map<String, Object> params, Class<T> clazz) {
        checkParams(model, stmtId, params);
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        try {
            if (logger.isDebugEnabled())
                logger.debug(String.format("\nsql-> [%s.%s] \n params-> [%s] \n execsql -> %s",
                        model, stmtId, params, execSql));
            Stopwatch stopwatch = Stopwatch.createStarted();
            T result = getQueryTemplate().queryForObject(execSql, params, clazz);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForObject [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, result, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "queryForEntity", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    public CompletableFuture<AsyncResult> queryListSupplyAsync(final String model, final String stmtId, final Map<String, Object> params) {
        checkParams(model, stmtId, params);
        final String execSql = statementFactory.getExecSql(model, stmtId, params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("\nsql-> [%s.%s] \n params-> [%s] \n execsql -> %s",
                    model, stmtId, params, execSql));
        return CompletableFuture.supplyAsync(() -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<Map<String, Object>> result = getQueryTemplate().queryForList(execSql, params);
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryListSupplyAsync [%s,%s result.size %s and elapsed %s ms]",
                        model, stmtId, CollectionUtils.isEmpty(result) ? 0 : result.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return AsyncResult.creat4ListMap(model, stmtId, params, CollectionUtils.isEmpty(result) ? null : result);
        });
    }

    public CompletableFuture<AsyncResult> queryMapSupplyAsync(final String model, final String stmtId, final Map<String, Object> params) {
        checkParams(model, stmtId, params);
        final String execSql = statementFactory.getExecSql(model, stmtId, params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("\nsql-> [%s.%s] \n params-> [%s] \n execsql -> %s",
                    model, stmtId, params, execSql));
        return CompletableFuture.supplyAsync(() -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<Map<String, Object>> result = getQueryTemplate().queryForList(execSql, params);
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryMapSupplyAsync [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, result, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            if (CollectionUtils.isEmpty(result)) return AsyncResult.creat4Map(model, stmtId, params, null);
            return AsyncResult.creat4Map(model, stmtId, params, result.get(0));
        });
    }

    public <T> Optional<T> queryForObject(String model, String stmtId, String key, Object value, Class<T> clazz) {
        Map<String, Object> params = Maps.newHashMap();
        if (value != null) params.put(Strings.isNullOrEmpty(key) ? "value" : key, value);
        return this.queryForObject(model, stmtId, params, clazz);
    }

    // 验证基本的程序合法性
    private void checkParams(String model, String stmtId, Map<String, Object> params) {
        SQLStatement getSqlMeta = statementFactory.loadStmtById(model, stmtId);
        getSqlMeta.handleParams(params);
    }

    private NamedParameterJdbcTemplate getQueryTemplate() {
        return getNamedParameterJdbcTemplate();
    }

    private SQLStatementFactory statementFactory;

    public void setStatementFactory(SQLStatementFactory statementFactory) {
        this.statementFactory = statementFactory;
    }
}
