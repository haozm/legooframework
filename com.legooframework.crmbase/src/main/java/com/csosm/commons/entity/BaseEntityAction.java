package com.csosm.commons.entity;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.BusEvent;
import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntityFactory;
import com.csosm.module.base.cache.GuavaCache;
import com.csosm.module.base.cache.GuavaCacheManager;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseEntityAction<T extends BaseEntity> extends NamedParameterJdbcDaoSupport {

    private static final Logger logger = LoggerFactory.getLogger(BaseEntityAction.class);

    private final String model;
    private final String cacheName;
    private final static int GENERATEDKEY_INT = 1;
    private final static int GENERATEDKEY_LONG = 2;

    /**
     * 将对象放入缓存
     *
     * @param entity
     */
    protected void put(String id, Object entity) {
        if (!getCache().isPresent()) {
            if (logger.isDebugEnabled())
                logger.debug("缓存不存在");
            return;
        }
        String cacheKey = getCacheKey(id);
        GuavaCache cache = getCache().get();
        if (cache.getIfPresent(cacheKey) != null)
            cache.invalidate(cacheKey);
        cache.put(cacheKey, entity);
    }

    /**
     * 获取默认缓存key
     *
     * @param id
     * @return
     */
    private final String getCacheKey(String id) {
        return String.format("%s_id_%s", getModel(), id);
    }

    /**
     * 将缓存中的数据失效
     *
     * @param id
     */
    protected void invalidate(String id) {
        if (!getCache().isPresent()) {
            if (logger.isDebugEnabled())
                logger.debug("缓存不存在");
            return;
        }
        String cacheKey = getCacheKey(id);
        GuavaCache cache = getCache().get();
        if (cache.getIfPresent(cacheKey) != null)
            cache.invalidate(cacheKey);
    }

    @SuppressWarnings("unchecked")
    public Optional<T> findById(Object id) {
        if (null == id) return Optional.absent();
        final String cache_key = String.format("%s_id_%s", getModel(), id);
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().getIfPresent(cache_key);
            if (cache_val != null) {
                T val = (T) cache_val;
                if (logger.isDebugEnabled())
                    logger.debug(String.format("findById(%s) Touch from Cache by key=%s.", id, cache_key));
                return Optional.of(val);
            }
        }
        T res = selectById(id);
        if (null != res && getCache().isPresent()) {
            getCache().get().put(cache_key, res);
        }
        return Optional.fromNullable(res);
    }

    public T loadById(Object id) {
        Optional<T> optional = findById(id);
        Preconditions.checkState(optional.isPresent(), "不存在Id=%s 对应的实体", id);
        return optional.get();
    }

    protected String getModel() {
        return model;
    }

    protected BaseEntityAction(String model, String cacheName) {
        this.model = model;
        this.cacheName = cacheName;
    }

    protected T selectById(Object id) {
        return selectById(id, "findById");
    }

    private T selectById(Object id, String sqlId) {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("id", id);
        try {
            T building = getJdbc().query(sqlMetaEntityFactory.getExecSql(getModel(), sqlId, paramMap),
                    paramMap, getResultSetExtractor());
            if (logger.isDebugEnabled())
                logger.debug(String.format("<%s> selectById(%s) return entity is %s", getModel(), id, building));
            if (building == null) return null;
            return building;
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("selectById(%s)Incorrect result size: expected 1, actual 0", id));
            return null;
        }
    }

    protected String getExecSql(String stmtId, Map<String, Object> params) {
        return sqlMetaEntityFactory.getExecSql(getModel(), stmtId, params);
    }

    protected String getExecSql(String stmtId) {
        return sqlMetaEntityFactory.getExecSql(getModel(), stmtId, null);
    }

    protected Optional<GuavaCache> getCache() {
        if (Strings.isNullOrEmpty(cacheName)) return Optional.absent();
        return guavaCacheManager.getCache(cacheName);
    }

    protected NamedParameterJdbcTemplate getJdbc() {
        return super.getNamedParameterJdbcTemplate();
    }

    protected abstract ResultSetExtractor<T> getResultSetExtractor();

    private GuavaCacheManager guavaCacheManager;

    private AsyncEventBus asyncEventBus;

    public void setAsyncEventBus(AsyncEventBus asyncEventBus) {
        this.asyncEventBus = asyncEventBus;
    }

    protected SqlMetaEntityFactory sqlMetaEntityFactory;

    public void setSqlMetaEntityFactory(SqlMetaEntityFactory sqlMetaEntityFactory) {
        this.sqlMetaEntityFactory = sqlMetaEntityFactory;
    }

    public void setGuavaCacheManager(GuavaCacheManager guavaCacheManager) {
        this.guavaCacheManager = guavaCacheManager;
    }

    protected Object insertSingleWithGeneratedKey(String stmtId, Map<String, Object> params, final BatchSetter setter,
                                                  int generatedkeyType) {
        final String exec_sql = getExecSql(stmtId, params);
        KeyHolder generatedKey = new GeneratedKeyHolder();
        Objects.requireNonNull(getJdbcTemplate()).update(connection -> {
            PreparedStatement ps = connection.prepareStatement(exec_sql, Statement.RETURN_GENERATED_KEYS);
            setter.setValues(ps);
            return ps;
        }, generatedKey);
        if (generatedKey.getKey() == null)
            throw new NullPointerException("系统无自增长健,返回为空值...");
        if (GENERATEDKEY_INT == generatedkeyType) {
            return generatedKey.getKey().intValue();
        } else if (GENERATEDKEY_LONG == generatedkeyType) {
            return generatedKey.getKey().longValue();
        } else {
            throw new IllegalArgumentException(String.format("非法的参数 generatedkeyType = %s,取值范围是:[1,2]", generatedkeyType));
        }
    }

    protected void logProxy(BusEvent event) {
        try {
            if (asyncEventBus != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof LoginUserContext) event.setLoginUser((LoginUserContext) principal);
                asyncEventBus.post(event);
            } else {
                logger.warn("尚未注入 异步事件广播机制，存储操作日志失败...");
            }
        } catch (Exception e) {
            logger.warn(" 异步事件广播机制，操作失败...");
        }
    }
}
