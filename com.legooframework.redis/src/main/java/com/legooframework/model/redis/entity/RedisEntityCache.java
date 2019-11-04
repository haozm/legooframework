package com.legooframework.model.redis.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RedisEntityCache {

    private static final Logger logger = LoggerFactory.getLogger(RedisEntityCache.class);

    public RedisEntityCache(RedisTemplate<String, Object> redisTemplate) {
        if (null == redisTemplate)
            logger.warn("RedisCacheSupport cannot init redisTemplate ......so can't be word....");
        if (null != redisTemplate) {
            this.redisTemplate = redisTemplate;
            this.opsForHash = this.redisTemplate.opsForHash();
        } else {
            this.opsForHash = null;
            this.redisTemplate = null;
        }
    }

    public void cacheEnityNotNull(BaseEntity<?> entity) {
        Preconditions.checkArgument(null != entity, "BaseEntity<?> entity 不可以为空值");
        getCache().ifPresent(hash -> hash.put(entity.getClass().getSimpleName(),
                String.valueOf(entity.getId()), entity.toString()));
    }

    public void cacheEnity(BaseEntity<?> entity) {
        if (null == entity) return;
        getCache().ifPresent(hash -> hash.put(entity.getClass().getSimpleName(),
                String.valueOf(entity.getId()), entity.toString()));
    }

    public void cacheEnity(BaseEntity<?> entity, int timeout, TimeUnit unit) {
        if (null == entity) return;
        getRedisTemplate().ifPresent(redis -> {
            String key = entity.getClass().getSimpleName();
            boolean haskey = redis.hasKey(key);
            redis.opsForHash().put(key, String.valueOf(entity.getId()), entity.toString());
            if (!haskey) redis.expire(key, timeout, unit);
        });
    }

    public void cacheEnity(Optional<BaseEntity<?>> optional) {
        getCache().ifPresent(hash -> optional.ifPresent(opt -> hash.put(opt.getClass().getSimpleName(),
                String.valueOf(opt.getId()), opt.toString())));
    }

    public void clear(Class<? extends BaseEntity> clazz, Serializable id) {
        getCache().ifPresent(hash -> hash.delete(clazz.getSimpleName(), id));
    }

    public <T extends BaseEntity> Optional<String> getIfExits(Object id, Class<T> clazz) {
        if (!getCache().isPresent()) return Optional.empty();
        String serializer = getCache().get().get(clazz.getSimpleName(), String.valueOf(id));
        return Optional.ofNullable(serializer);
    }

    public void clear(Class<? extends BaseEntity> clazz, Serializable... ids) {
        if (ArrayUtils.isEmpty(ids)) return;
        getCache().ifPresent(cache -> cache.delete(clazz.getSimpleName(), (Object[]) ids));
    }

    public void clearAll(Class<? extends BaseEntity> clazz) {
        getRedisTemplate().ifPresent(redis -> redis.delete(clazz.getSimpleName()));
    }

    private Optional<HashOperations<String, String, String>> getCache() {
        return Optional.ofNullable(opsForHash);
    }

    private Optional<RedisTemplate<String, Object>> getRedisTemplate() {
        return Optional.ofNullable(redisTemplate);
    }

    private final HashOperations<String, String, String> opsForHash;
    private final RedisTemplate<String, Object> redisTemplate;

}
