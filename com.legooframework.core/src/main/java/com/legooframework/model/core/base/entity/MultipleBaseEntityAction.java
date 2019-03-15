package com.legooframework.model.core.base.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.jdbc.MultipleEntityDaoSupport;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.core.ResolvableType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public abstract class MultipleBaseEntityAction<T extends BaseEntity> extends MultipleEntityDaoSupport {

    private static final Logger logger = LoggerFactory.getLogger(MultipleBaseEntityAction.class);

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected MultipleBaseEntityAction(String cacheName) {
        this.cacheName = cacheName;
        this.entityClass = (Class<T>) ResolvableType.forClass(this.getClass())
                .as(MultipleBaseEntityAction.class).getGeneric(0).resolve();
        Preconditions.checkNotNull(entityClass, "无法获取 %s 对应的<T extends BaseEntity>泛型参数.");
        this.modelName = entityClass.getSimpleName();
    }
//
//    protected <T extends BatchSetter> int[][] batchUpdate(String stmtId, ParameterizedPreparedStatementSetter<T> pss,
//                                                          Collection<T> batchArgs, Object router) {
//        return super.batchUpdate(getStatementFactory(), getModelName(), stmtId, pss, batchArgs, router);
//    }

//    protected <T extends BatchSetter> CompletableFuture<int[][]> asyncBatchUpdate(String stmtId,
//                                                                                  ParameterizedPreparedStatementSetter<T> pss,
//                                                                                  Collection<T> batchArgs,
//                                                                                  Object router) {
//        return super.asyncBatchUpdate(getStatementFactory(), getModelName(), stmtId, pss, batchArgs, router);
//    }
//
//    protected <T extends BatchSetter> int[][] batchInsert(String stmtId, Collection<T> batchArgs, Object router) {
//        return super.batchInsert(getStatementFactory(), getModelName(), stmtId, batchArgs, router);
//    }
//
//    protected <T extends BatchSetter> CompletableFuture<int[][]> asyncBatchInsert(String stmtId,
//                                                                                  Collection<T> batchArgs,
//                                                                                  Object router) {
//        return super.asyncBatchInsert(getStatementFactory(), getModelName(), stmtId, batchArgs, router);
//    }

    protected Optional<List<Map<String, Object>>> queryForMapList(String stmtId, Map<String, Object> params, Object router) {
        String exec_sql = getStatementFactory().getExecSql(getModelName(), stmtId, params);
        NamedParameterJdbcTemplate jdbcTemplate = multipleDataSource.loadParamsTemplate(router);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(exec_sql, params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryForMapList(%s,%s) size is %s", getModelName(), stmtId,
                    CollectionUtils.isEmpty(mapList) ? 0 : mapList.size()));
        return Optional.ofNullable(CollectionUtils.isEmpty(mapList) ? null : mapList);
    }

    protected <T extends BaseEntity> Optional<T> queryForEntity(String stmtId, Map<String, Object> params,
                                                                RowMapper<T> rowMapper, Object router) {
        return super.queryForEntity(statementFactory, getModelName(), stmtId, params, rowMapper, router);
    }

    protected <T extends BaseEntity> Optional<List<T>> queryForEntities(String stmtId, Map<String, Object> params,
                                                                        RowMapper<T> rowMapper, Object router) {
        return super.queryForEntities(statementFactory, getModelName(), stmtId, params, rowMapper, router);
    }

    protected <T extends BaseEntity> CompletableFuture<List<T>> asyncQueryForEntities(String stmtId,
                                                                                      Map<String, Object> params,
                                                                                      RowMapper<T> rowMapper,
                                                                                      Object router) {
        return super.asyncQueryForList(getStatementFactory(), getModelName(), stmtId, params, rowMapper, router);
    }

    protected <T> Optional<T> queryForObject(String stmtId, Map<String, Object> paramMap, Class<T> clazz, Object router) {
        try {
            return super.queryForObject(getStatementFactory(), getModelName(), stmtId, paramMap, clazz, router);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    protected void cacheEntity(T entity) {
        if (entity == null) return;
        getCache().ifPresent(c -> c.put(cacheByIdKey(entity.getId()), entity));
    }

    protected void evictEntity(T entity) {
        if (entity == null) return;
        getCache().ifPresent(c -> c.evict(cacheByIdKey(entity.getId())));
    }

//    protected int updateAction(String stmtId, Map<String, Object> params, Object router) {
//        Preconditions.checkNotNull(stmtId);
//        Preconditions.checkState(statementFactory.contains(modelName, stmtId));
//        return super.update(statementFactory, modelName, stmtId, params, router);
//    }
//
//    protected int updateAction(T enity, String stmtId, Object router) {
//        Preconditions.checkNotNull(enity);
//        Preconditions.checkNotNull(stmtId);
//        Preconditions.checkState(statementFactory.contains(modelName, stmtId), "%s.%s 对应的SQL语句尚未定义...", modelName, stmtId);
//        return super.update(statementFactory, modelName, stmtId, enity, router);
//    }

    protected String getModelName() {
        return modelName;
    }

    protected Optional<Cache> getCache() {
        if (Strings.isNullOrEmpty(this.cacheName)) return Optional.empty();
        if (cacheManager == null) return Optional.empty();
        return Optional.ofNullable(cacheManager.getCache(cacheName));
    }

    protected abstract RowMapper<T> getRowMapper();

    protected String cacheByIdKey(Object id) {
        return String.format("%s_id_%s", modelName, id);
    }

    private final String cacheName;
    private final String modelName;

    private CaffeineCacheManager cacheManager;
    private ThreadPoolTaskExecutor executorService;
    private SQLStatementFactory statementFactory;

    @Override
    protected ExecutorService getExecutorService() {
        return executorService.getThreadPoolExecutor();
    }

    protected SQLStatementFactory getStatementFactory() {
        return statementFactory;
    }

    public void setCacheManager(CaffeineCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setExecutorService(ThreadPoolTaskExecutor executorService) {
        this.executorService = executorService;
    }

    public void setStatementFactory(SQLStatementFactory statementFactory) {
        this.statementFactory = statementFactory;
    }
}
