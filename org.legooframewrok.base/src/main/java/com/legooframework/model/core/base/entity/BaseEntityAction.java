package com.legooframework.model.core.base.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.EntityDaoSupport;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import com.legooframework.model.core.utils.ExceptionUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.core.ResolvableType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public abstract class BaseEntityAction<T extends BaseEntity> extends EntityDaoSupport {

    private static final Logger logger = LoggerFactory.getLogger(BaseEntityAction.class);

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected BaseEntityAction(String cacheName) {
        this.cacheName = cacheName;
        this.entityClass = (Class<T>) ResolvableType.forClass(this.getClass())
                .as(BaseEntityAction.class).getGeneric(0).resolve();
        Preconditions.checkNotNull(entityClass, "无法获取 %s 对应的<T extends BaseEntity>泛型参数.");
        this.modelName = entityClass.getSimpleName();
    }

    protected <T extends BatchSetter> int[][] batchUpdate(String stmtId, ParameterizedPreparedStatementSetter<T> pss,
                                                          Collection<T> batchArgs) {
        return super.batchUpdate(getStatementFactory(), getModelName(), stmtId, pss, batchArgs);
    }

    protected <T extends BatchSetter> int[][] batchInsert(String stmtId, Collection<T> batchArgs) {
        return super.batchInsert(getStatementFactory(), getModelName(), stmtId, batchArgs);
    }

    protected Optional<List<Map<String, Object>>> queryForMapList(String stmtId, Map<String, Object> paramMap) {
        LoginContext loginUser = LoginContextHolder.get();
        Map<String, Object> params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(paramMap))
            params.putAll(paramMap);
        params.putAll(loginUser.toParams());
        String exec_sql = getStatementFactory().getExecSql(getModelName(), stmtId, params);
        List<Map<String, Object>> mapList = getNamedParameterJdbcTemplate().queryForList(exec_sql, params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryForMapList(%s,%s) size is %s", getModelName(), stmtId,
                    CollectionUtils.isEmpty(mapList) ? 0 : mapList.size()));
        return Optional.ofNullable(CollectionUtils.isEmpty(mapList) ? null : mapList);
    }

    protected <T extends BaseEntity> Optional<T> queryForEntity(String stmtId, Map<String, Object> paramMap,
                                                                RowMapper<T> rowMapper) {
        return super.queryForEntity(statementFactory, getModelName(), stmtId, paramMap, rowMapper);
    }

    protected <T extends BaseEntity> Optional<List<T>> queryForEntities(String stmtId, Map<String, Object> paramMap,
                                                                        RowMapper<T> rowMapper) {
        return super.queryForEntities(statementFactory, getModelName(), stmtId, paramMap, rowMapper);
    }

    protected <T extends BaseEntity> CompletableFuture<List<T>> asyncQueryForEntities(String stmtId,
                                                                                      Map<String, Object> paramMap,
                                                                                      RowMapper<T> rowMapper) {
        return super.asyncQueryForList(getStatementFactory(), getModelName(), stmtId, paramMap, rowMapper);
    }

    protected <T> Optional<T> queryForObject(String stmtId, Map<String, Object> paramMap, Class<T> clazz) {
        try {
            return super.queryForObject(getStatementFactory(), getModelName(), stmtId, paramMap, clazz);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<T> findById(Object id) {
        Preconditions.checkNotNull(id, "in-param id can not be null");
        final String cache_key = cacheByIdKey(id);
        if (getCache().isPresent()) {
            Optional<T> cache_val = getCache().map(c -> c.get(cache_key, entityClass));
            if (cache_val.isPresent()) {
                if (logger.isTraceEnabled())
                    logger.trace(String.format("Hit cache_key = %s From Cache", cache_key));
                return cache_val;
            }
        }
        try {
            Map<String, Object> params = Maps.newHashMap();
            params.put("id", id);
            Optional<T> entity = super.queryForEntity(statementFactory, modelName, "findById",
                    params, getRowMapper());
            if (!entity.isPresent()) return Optional.empty();
            cacheEntity(entity.get());
            return entity;
        } catch (Exception e) {
            String err_msg = String.format("%s(%s) has error", "findById", id);
            throw ExceptionUtil.handleException(e, err_msg, logger);
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

    public T loadById(Object id) {
        Optional<T> entity = findById(id);
        if (!entity.isPresent())
            throw new EntityNotExitsException(entityClass, id);
        return entity.get();
    }

    protected long queryForLong(String sql, Long defval) {
        Long value = getJdbcTemplate().queryForObject(sql, Long.class);
        return value == null ? defval : value;
    }

    public int deleteById(Object id) {
        Optional<T> exits = findById(id);
        if (!exits.isPresent()) return 0;
        LoginContext loginContext = LoginContextHolder.get();
        exits.get().setEditor(loginContext.getLoginId());
        int result = super.update(statementFactory, modelName, "deleteById", exits.get());
        if (1 == result) evictEntity(exits.get());
        return result;
    }

    public int deleteByEntity(T entity) {
        Preconditions.checkNotNull(entity);
        return super.update(statementFactory, modelName, "deleteById", entity);
    }

    protected int updateAction(String stmtId, Map<String, Object> paramMap) {
        Preconditions.checkNotNull(stmtId);
        Preconditions.checkState(statementFactory.contains(modelName, stmtId));
        return super.update(statementFactory, modelName, stmtId, paramMap);
    }

    protected int updateAction(T enity, String stmtId) {
        Preconditions.checkNotNull(enity);
        Preconditions.checkNotNull(stmtId);
        Preconditions.checkState(statementFactory.contains(modelName, stmtId), "%s.%s 对应的SQL语句尚未定义...", modelName, stmtId);
        return super.update(statementFactory, modelName, stmtId, enity);
    }

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
