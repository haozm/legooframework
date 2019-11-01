package com.legooframework.model.redis.entity;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisTemplateSupport {

    public RedisTemplateSupport(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final RedisTemplate redisTemplate;

}
