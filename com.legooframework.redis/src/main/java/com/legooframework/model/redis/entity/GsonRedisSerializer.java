package com.legooframework.model.redis.entity;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class GsonRedisSerializer implements RedisSerializer<Object> {

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        String className = o.getClass().getName();
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return null;
    }
}
