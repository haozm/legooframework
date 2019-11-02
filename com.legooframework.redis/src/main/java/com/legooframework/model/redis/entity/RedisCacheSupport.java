package com.legooframework.model.redis.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

public class RedisCacheSupport {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheSupport.class);

    public RedisCacheSupport(RedisTemplate redisTemplate) {
        if (null == redisTemplate)
            logger.warn("RedisCacheSupport cannot init redisTemplate ......so can't be word....");
        this.redisTemplate = redisTemplate;
    }

    public void cacheEnity(BaseEntity<?> entity) {
        Preconditions.checkArgument(null != entity, "BaseEntity<?> entity 不可以为空值");
        getRedisTemplate().ifPresent(redis -> {
            
        });
    }

    private Optional<RedisTemplate> getRedisTemplate() {
        return Optional.ofNullable(redisTemplate);
    }

    private final RedisTemplate redisTemplate;

}
