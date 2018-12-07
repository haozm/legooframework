package com.legooframework.model.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.legooframework.model.base.entity.BaseEntity;
import com.legooframework.model.base.runtime.LoginContextHolder;
import com.legooframework.model.jdbc.sqlengine.SQLStatementFactory;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class EntityDaoSupport extends NamedParameterJdbcDaoSupport {

    private static final Logger logger = LoggerFactory.getLogger(EntityDaoSupport.class);

    private NamedParameterJdbcTemplate getTemplate() {
        return getNamedParameterJdbcTemplate();
    }

    protected <T extends BatchSetter> int[][] batchUpdate
            (SQLStatementFactory statementFactory, String model, String stmtId, Collection<T> batchArgs) {
        Preconditions.checkNotNull(statementFactory);
        Preconditions.checkArgument(!CollectionUtils.isEmpty(batchArgs));
        Map<String, Object> params = LoginContextHolder.get().toParams();
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("batchUpdate", model, stmtId, params, execSql));
            Preconditions.checkNotNull(getJdbcTemplate(), "JdbcTemplate Not Be init...");
            int[][] result = getJdbcTemplate().batchUpdate(execSql, batchArgs, 2048,
                    (ps, argument) -> argument.setValues(ps));
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("batchUpdate [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, batchArgs.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return result;
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "batchUpdate", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    protected <T> Optional<T> queryForObject(SQLStatementFactory statementFactory, String model, String stmtId,
                                             Map<String, Object> paramMap, Class<T> clazz) {
        Preconditions.checkNotNull(statementFactory);
        Map<String, Object> params = LoginContextHolder.get().toParams();
        if (MapUtils.isNotEmpty(paramMap))
            params.putAll(paramMap);
        String execSql = statementFactory.getExecSql(model, stmtId, params);

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("queryForObject", model, stmtId, params, execSql));
            T result = getTemplate().queryForObject(execSql, params, clazz);
            stopwatch.stop();

            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForObject [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, result, stopwatch.elapsed(TimeUnit.MILLISECONDS)));

            return Optional.ofNullable(result);
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "queryForObject", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    private String buildLog(String method, String model, String stmtId, Map<String, Object> params, String execSql) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[method=").append(method).append(", model=")
                .append(model).append(", stmtId=").append(stmtId).append("]\n")
                .append("[params=").append(params).append("\n")
                .append("[execSQL=").append(execSql);
        return sb.toString();
    }

    protected <T extends BaseEntity> Optional<T> queryForEntity(SQLStatementFactory statementFactory,
                                                                String model, String stmtId,
                                                                Map<String, Object> paramMap,
                                                                RowMapper<T> rowMapper) {
        Preconditions.checkNotNull(statementFactory);
        Map<String, Object> params = LoginContextHolder.get().toParams();
        if (MapUtils.isNotEmpty(paramMap))
            params.putAll(paramMap);
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("queryForEntity", model, stmtId, params, execSql));
            T result = getTemplate().queryForObject(execSql, params, rowMapper);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForEntity [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, result, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException er) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForEntity [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, 0, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.empty();
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "queryForEntity", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    protected <T extends BaseEntity> Optional<List<T>> queryForEntities(SQLStatementFactory statementFactory,
                                                                        String model, String stmtId,
                                                                        Map<String, Object> paramMap,
                                                                        RowMapper<T> rowMapper) {
        Preconditions.checkNotNull(statementFactory);
        Map<String, Object> params = LoginContextHolder.get().toParams();
        if (MapUtils.isNotEmpty(paramMap))
            params.putAll(paramMap);

        String execSql = statementFactory.getExecSql(model, stmtId, params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("queryForEntities", model, stmtId, params, execSql));
            List<T> result = getTemplate().query(execSql, params, rowMapper);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForEntities [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, CollectionUtils.isEmpty(result) ? 0 : result.size(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.ofNullable(CollectionUtils.isEmpty(result) ? null : result);
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "queryForEntities", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    protected <T extends BaseEntity> CompletableFuture<List<T>> asyncQueryForList(SQLStatementFactory statementFactory,
                                                                                  String model, String stmtId,
                                                                                  Map<String, Object> paramMap,
                                                                                  RowMapper<T> rowMapper) {
        Preconditions.checkNotNull(statementFactory);
        Preconditions.checkNotNull(getExecutorService(), "线程执行器不可以为空值...");
        final Map<String, Object> params = LoginContextHolder.get().toParams();
        if (MapUtils.isNotEmpty(paramMap))
            params.putAll(paramMap);
        final String execSql = statementFactory.getExecSql(model, stmtId, params);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                if (logger.isDebugEnabled())
                    logger.debug(buildLog("asyncQueryForList", model, stmtId, params, execSql));
                List<T> result = getTemplate().query(execSql, params, rowMapper);
                if (logger.isDebugEnabled())
                    logger.debug(String.format("asyncQueryForList [%s,%s result is %s] kill time %s",
                            model, stmtId, CollectionUtils.isEmpty(result) ? 0 : result.size(),
                            stopwatch.elapsed(TimeUnit.MILLISECONDS)));
                return CollectionUtils.isEmpty(result) ? null : result;
            } catch (DataAccessException e) {
                String err_msg = String.format("%s(%s,%s) whit %s has error", "asyncQueryForList", model, stmtId, params);
                logger.error(err_msg, e);
                throw new ExecJdbcSqlException(err_msg, e);
            }
        }, getExecutorService());
    }

    @SuppressWarnings("unchecked")
    protected <T extends BaseEntity> int update(SQLStatementFactory statementFactory, String model,
                                                String stmtId, T entity) {
        Preconditions.checkNotNull(statementFactory);
        Map<String, Object> params = entity.toParamMap();
        params.putAll(LoginContextHolder.get().toParams());
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        return update(model, stmtId, execSql, params);
    }


    protected int update(SQLStatementFactory statementFactory, String model, String stmtId,
                         Map<String, Object> paramMap) {
        Preconditions.checkNotNull(statementFactory);
        Map<String, Object> params = LoginContextHolder.get().toParams();
        params.putAll(paramMap);
        String execSql = statementFactory.getExecSql(model, stmtId, params);
        return update(model, stmtId, execSql, params);
    }

    private int update(String model, String stmtId, String execSql, Map<String, Object> params) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("update", model, stmtId, params, execSql));
            int result = getTemplate().update(execSql, params);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("update [%s,%s] result is %s and elapsed %s ms",
                        model, stmtId, result, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return result;
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "update", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    protected abstract ExecutorService getExecutorService();
}
