package com.legooframework.model.core.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class MultipleEntityDaoSupport {

    private static final Logger logger = LoggerFactory.getLogger(MultipleEntityDaoSupport.class);

    protected <T> Optional<T> queryForObject(SQLStatementFactory statementFactory, String model, String stmtId,
                                             Map<String, Object> params, Class<T> clazz,
                                             Object router) {
        Preconditions.checkNotNull(statementFactory, "参数 SQLStatementFactory statementFactory 不可以为空值...");

        String execSql = statementFactory.getExecSql(model, stmtId, params);
        NamedParameterJdbcTemplate jdbcTemplate = multipleDataSource.loadParamsTemplate(router);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("queryForObject", model, stmtId, params, execSql, router));
            T result = jdbcTemplate.queryForObject(execSql, params, clazz);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForObject [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, result, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e1) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForObject [%s,%s result is %s and elapsed %s ms]",
                        model, stmtId, 0, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.empty();
        } catch (DataAccessException e) {
            String err_msg = String.format("%s(%s,%s) whit %s has error", "queryForObject", model, stmtId, params);
            logger.error(err_msg, e);
            throw new ExecJdbcSqlException(err_msg, e);
        }
    }

    private String buildLog(String method, String model, String stmtId, Map<String, Object> params, String execSql,
                            Object router) {
        return String.format("\n[router=%s, method=%s, model=%s, stmtId=%s ]\n[params=%s]\n[execSQL=%s]", router,
                method, model, stmtId, params, execSql);
    }

    protected <T extends BaseEntity> Optional<T> queryForEntity(SQLStatementFactory statementFactory,
                                                                String model, String stmtId,
                                                                Map<String, Object> params,
                                                                RowMapper<T> rowMapper,
                                                                Object router) {
        Preconditions.checkNotNull(statementFactory, "参数 SQLStatementFactory statementFactory 不可以为空值...");

        String execSql = statementFactory.getExecSql(model, stmtId, params);
        NamedParameterJdbcTemplate jdbcTemplate = multipleDataSource.loadParamsTemplate(router);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("queryForEntity", model, stmtId, params, execSql, router));
            T result = jdbcTemplate.queryForObject(execSql, params, rowMapper);
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
                                                                        Map<String, Object> params,
                                                                        RowMapper<T> rowMapper,
                                                                        Object router) {
        Preconditions.checkNotNull(statementFactory, "参数 SQLStatementFactory statementFactory 不可以为空值...");

        NamedParameterJdbcTemplate jdbcTemplate = multipleDataSource.loadParamsTemplate(router);

        String execSql = statementFactory.getExecSql(model, stmtId, params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (logger.isDebugEnabled())
                logger.debug(buildLog("queryForEntities", model, stmtId, params, execSql, router));
            List<T> result = jdbcTemplate.query(execSql, params, rowMapper);
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
                                                                                  Map<String, Object> params,
                                                                                  RowMapper<T> rowMapper,
                                                                                  Object router) {
        Preconditions.checkNotNull(statementFactory, "参数 SQLStatementFactory statementFactory 不可以为空值...");
        Preconditions.checkNotNull(getExecutorService(), "线程执行器不可以为空值...");
        NamedParameterJdbcTemplate jdbcTemplate = multipleDataSource.loadParamsTemplate(router);
        final String execSql = statementFactory.getExecSql(model, stmtId, params);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                if (logger.isDebugEnabled())
                    logger.debug(buildLog("asyncQueryForList", model, stmtId, params, execSql, router));
                List<T> result = jdbcTemplate.query(execSql, params, rowMapper);
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

    protected MultipleDataSource multipleDataSource;

    public void setMultipleDataSource(MultipleDataSource multipleDataSource) {
        this.multipleDataSource = multipleDataSource;
    }

    protected abstract ExecutorService getExecutorService();
}
