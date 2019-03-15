package com.csosm.module.query;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.jdbc.UnknownColumnException;
import com.csosm.commons.jdbc.sqlcfg.ColumnMeta;
import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntity;
import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntityFactory;
import com.csosm.commons.mvc.ServletRequestHelper;
import com.csosm.module.base.entity.RoleEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.query.entity.PagingResult;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class QueryEngineService extends NamedParameterJdbcDaoSupport implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(QueryEngineService.class);

    public Long queryForCount(final String model, final String stmtId, Map<String, Object> paramMap) {
        final Map<String, Object> _paramMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(paramMap)) _paramMap.putAll(paramMap);
        logParams(model, stmtId, _paramMap);
        final String execSql_count = sqlMetaEntityFactory.getExecSql(model, String.format("%s_count", stmtId), _paramMap);
        try {
            Long count = getNamedParameterJdbcTemplate().queryForObject(execSql_count, _paramMap, Long.class);
            if (count == null) count = 0L;
            return count;
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("queryForCount [%s.%s] is return null for it...", model, stmtId));
            return 0L;
        }
    }

    public PagingResult queryForPage(final String model, final String stmtId, int pageNum, int pageSize,
                                     Map<String, Object> paramMap) {
        // 保护措施......
        int _pageSize = pageSize > 100 ? 100 : pageSize;
        int _size = _pageSize <= 0 ? 20 : _pageSize;
        long offset = pageNum <= 1 ? 0 : (pageNum - 1) * _size;

        final Map<String, Object> _paramMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(paramMap)) _paramMap.putAll(paramMap);
        _paramMap.put("offset", offset);
        _paramMap.put("rows", _size);

        logParams(model, stmtId, _paramMap);

        final String execSql_count = sqlMetaEntityFactory.getExecSql(model, String.format("%s_count", stmtId), _paramMap);

        Stopwatch stopwatch = Stopwatch.createStarted();
        Future<Long> count_future = executor.submit(new Callable<Long>() {
            public Long call() throws Exception {
                try {
                    Long count = getNamedParameterJdbcTemplate().queryForObject(execSql_count, _paramMap, Long.class);
                    if (count == null) count = 0L;
                    return count;
                } catch (EmptyResultDataAccessException e) {
                    logger.warn(String.format("queryForCount [%s.%s] is return null for it...", model, stmtId));
                    return 0L;
                }
            }
        });

        final String execSql = sqlMetaEntityFactory.getExecSql(model, stmtId, _paramMap);
        Future<Optional<List<Map<String, Object>>>> list_future = executor.submit(new Callable<Optional<List<Map<String, Object>>>>() {
            public Optional<List<Map<String, Object>>> call() throws Exception {
                List<Map<String, Object>> result = getNamedParameterJdbcTemplate().queryForList(execSql, _paramMap);
                if (CollectionUtils.isEmpty(result)) return Optional.absent();
                return Optional.of(result);
            }
        });


        try {
            Long count = count_future.get(5, TimeUnit.MINUTES);
            Optional<List<Map<String, Object>>> list = list_future.get(5, TimeUnit.MINUTES);
            if (count == 0L) {
                return PagingResult.emptyPagingResult("", "");
            }
            if (logger.isInfoEnabled()) {
                logger.info(String.format("list data cost about [%s] MILLISECONDS res is %s",
                        stopwatch.elapsed(TimeUnit.MILLISECONDS), list.isPresent() ? list.get().size() : 0));
            }
            return new PagingResult(model, stmtId, count, pageNum, pageSize, list.isPresent() ? list.get() : null);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<List<Map<String, Object>>> queryForList(String execSql, Map<String, Object> paramMap) {
        Map<String, Object> _map = Maps.newHashMap();
        if (MapUtils.isNotEmpty(paramMap)) _map.putAll(paramMap);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            List<Map<String, Object>> resultSet = getNamedParameterJdbcTemplate().queryForList(execSql, _map);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForList [%s result's size is %s and elapsed %s ms]",
                        execSql, CollectionUtils.isEmpty(resultSet) ? 0 : resultSet.size(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.fromNullable(CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        } catch (Exception e) {
            logger.error("执行 SQL 发生异常", e);
            if (e instanceof BadSqlGrammarException) {
                Throwable cause = e.getCause();
                String ee = cause.getMessage();
                if (StringUtils.startsWith(ee, "Unknown column"))
                    throw new UnknownColumnException(ee, cause);
            }
            throw new RuntimeException(String.format("执行查询[%s]发生异常。", execSql));
        }
    }

    public Optional<List<Map<String, Object>>> queryForList(String model, String stmtId, Map<String, Object> paramMap) {
        Map<String, Object> _map = Maps.newHashMap();
        if (MapUtils.isNotEmpty(paramMap)) _map.putAll(paramMap);
        String execSql = sqlMetaEntityFactory.getExecSql(model, stmtId, _map);
        logParams(model, stmtId, _map);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            List<Map<String, Object>> resultSet = getNamedParameterJdbcTemplate().queryForList(execSql, _map);
            stopwatch.stop();
            if (logger.isDebugEnabled())
                logger.debug(String.format("queryForList [%s,%s result's size is %s and elapsed %s ms]",
                        model, stmtId, CollectionUtils.isEmpty(resultSet) ? 0 : resultSet.size(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            // log("queryForList", model, stmtId, _map);
            return Optional.fromNullable(CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        } catch (Exception e) {
            logger.error("执行 SQL 发生异常", e);
            if (e instanceof BadSqlGrammarException) {
                Throwable cause = e.getCause();
                String ee = cause.getMessage();
                if (StringUtils.startsWith(ee, "Unknown column"))
                    throw new UnknownColumnException(ee, cause);
            }
            throw new RuntimeException(String.format("执行查询[%s.%s]发生异常。", model, stmtId));
        }
    }

    public Optional<List<Map<String, Object>>> queryForList(Map<String, Object> paramMap) {
        Preconditions.checkArgument(MapUtils.isNotEmpty(paramMap));
        String model = MapUtils.getString(paramMap, "modelName");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(model), "缺少 key =modelName 对应的赋值");
        String stmtId = MapUtils.getString(paramMap, "stmtId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "缺少 key =stmtId 对应的赋值");
        return queryForList(model, stmtId, paramMap);
    }

    public Optional<List<ColumnMeta>> getColumnMetas(String model, String stmtId) {
        SqlMetaEntity sqlMetaEntity = sqlMetaEntityFactory.getSqlMetaEntity(model, stmtId);
        return sqlMetaEntity.getColumnMetas();
    }

    public List<ColumnMeta> loadColumnMetas(String model, String stmtId) {
        SqlMetaEntity sqlMetaEntity = sqlMetaEntityFactory.getSqlMetaEntity(model, stmtId);
        Optional<List<ColumnMeta>> res = sqlMetaEntity.getColumnMetas();
        Preconditions.checkState(res.isPresent(), "未定义model=%s,stmtId=%s 相关的列标题", model, stmtId);
        return res.get();
    }

    Optional<List<Map<String, Object>>> query4ReportList(String modelName, String stmtId, Map<String, String> requestBody,
                                                         LoginUserContext loginUser) {
        Preconditions.checkState(loginUser.getCompany().isPresent(), "员工所在公司为空,无法执行后续操作...");
        Map<String, Object> params = loginUser.toMap();
        Optional<RoleEntity> role_opt = loginUser.getMaxPowerRole();
        Preconditions.checkState(role_opt.isPresent(), "当前账户账户无角色信息...,无法执行后续操作...");
        RoleEntity role = role_opt.get();

        Optional<Map<String, Object>> dynamic_query_map = ServletRequestHelper.parseQueryParams(requestBody);
        params.put("has_dynamic_params", dynamic_query_map.isPresent());
        if (MapUtils.getInteger(params, "storeId") != null) {
            Integer storeId = MapUtils.getInteger(params, "storeId");
            StoreEntity store = appCtx.getBean(StoreEntityAction.class).loadById(storeId);
            params.put("MSG_COM_STORE", String.format("MSG_%s_%s", loginUser.getCompany().get().getId(), store.getId()));
        }
//        ints_search_orgIds
//        ints_search_storeIds
//        ints_search_sgIds
        return queryForList(modelName, stmtId, params);
    }

    PagingResult query4ReportPages(String modelName, String stmtId, Map<String, String> requestBody,
                                   int pageNum, int pageSize, LoginUserContext loginUser) {
        Preconditions.checkState(loginUser.getCompany().isPresent(), "员工所在公司为空,无法执行后续操作...");
        Map<String, Object> params = loginUser.toMap();
        Optional<Map<String, Object>> dynamic_query_map = ServletRequestHelper.parseQueryParams(requestBody);
        params.put("has_dynamic_params", dynamic_query_map.isPresent());
        if (dynamic_query_map.isPresent()) params.putAll(dynamic_query_map.get());
        if (MapUtils.getInteger(params, "storeId") != null) {
            Integer storeId = MapUtils.getInteger(params, "storeId");
            StoreEntity store = appCtx.getBean(StoreEntityAction.class).loadById(storeId);
            params.put("MSG_COM_STORE", String.format("MSG_%s_%s", loginUser.getCompany().get().getId(), store.getId()));
        }
        return queryForPage(modelName, stmtId, pageNum, pageSize, params);
    }

    public <T> Optional<T> queryForObject(final String model, final String stmtId, final Map<String, Object> params,
                                          final Class<T> requiredType) {
        final String execSql = sqlMetaEntityFactory.getExecSql(model, stmtId, params);
        logParams(model, stmtId, params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            T res = getNamedParameterJdbcTemplate().queryForObject(execSql, params, requiredType);
            if (logger.isDebugEnabled())
                if (logger.isDebugEnabled())
                    logger.debug(String.format("queryForObject [%s,%s result is %s and elapsed %s ms]",
                            model, stmtId, res, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return Optional.fromNullable(res);
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("queryForObject [%s.%s] is return null for it...", model, stmtId));
            return Optional.absent();
        }
    }

    public <T> Optional<List<T>> queryForList(String model, String stmtId, Map<String, Object> paramMap, Class<T> elementType) {
        String execSql = sqlMetaEntityFactory.getExecSql(model, stmtId, paramMap);
        Stopwatch stopwatch = Stopwatch.createStarted();
        logParams(model, stmtId, paramMap);
        List<T> resultSet = getNamedParameterJdbcTemplate().queryForList(execSql, paramMap, elementType);
        stopwatch.stop();
        if (logger.isDebugEnabled())
            logger.debug(
                    String.format("queryForList[%s][%s,%s result's size is %s and elapsed %s ms]",
                            elementType, model, stmtId,
                            CollectionUtils.isEmpty(resultSet) ? 0 : resultSet.size(),
                            stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        return Optional.fromNullable(CollectionUtils.isEmpty(resultSet) ? null : resultSet);
    }

    public Optional<Map<String, Object>> queryForMap(final String model, final String stmtId, final Map<String, Object> params) {
        final String execSql = sqlMetaEntityFactory.getExecSql(model, stmtId, params);
        logParams(model, stmtId, params);
        try {
            Map<String, Object> map = getNamedParameterJdbcTemplate().queryForMap(execSql, params);
            return Optional.fromNullable(map);
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("Query [%s.%s] is return null for it...", model, stmtId));
            return Optional.absent();
        }
    }

    public ListenableFuture<Optional<List<Map<String, Object>>>> asyncQuery4List(final String model, final String sqlId,
                                                                                 final Map<String, Object> params) {
        return getExitingExecutorService().submit(new Callable<Optional<List<Map<String, Object>>>>() {
            @Override
            public Optional<List<Map<String, Object>>> call() throws Exception {
                String sql = sqlMetaEntityFactory.getExecSql(model, sqlId, params);
                List<Map<String, Object>> res = getNamedParameterJdbcTemplate().queryForList(sql, params);
                return Optional.fromNullable(CollectionUtils.isEmpty(res) ? null : res);
            }
        });
    }

    private ListeningExecutorService listeningExecutorService;

    private ThreadPoolTaskExecutor executor;

    private ApplicationContext appCtx;

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    private SqlMetaEntityFactory sqlMetaEntityFactory;

    public void setSqlMetaEntityFactory(SqlMetaEntityFactory sqlMetaEntityFactory) {
        this.sqlMetaEntityFactory = sqlMetaEntityFactory;
    }

    private synchronized ListeningExecutorService getExitingExecutorService() {
        if (listeningExecutorService != null) return listeningExecutorService;
        listeningExecutorService = MoreExecutors.listeningDecorator(executor.getThreadPoolExecutor());
        return listeningExecutorService;
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    private void logParams(String model, String stmtId, Map<String, Object> paramMap) {
        if (logger.isDebugEnabled() && MapUtils.isNotEmpty(paramMap)) {
            if (paramMap.containsKey("ALL_STORES") || paramMap.containsKey("SUB_STORES")) {
                Object ALL_STORES = paramMap.remove("ALL_STORES");
                Object SUB_STORES = paramMap.remove("SUB_STORES");
                logger.debug(String.format("%s.%s -> params %s", model, stmtId, paramMap));
                paramMap.put("ALL_STORES", ALL_STORES);
                paramMap.put("SUB_STORES", SUB_STORES);
            } else {
                logger.debug(String.format("%s.%s -> params %s", model, stmtId, paramMap));
            }
        }
    }
}
