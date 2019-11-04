package com.legooframework.model.redis.entity;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class MultipleSerializer implements RedisSerializer<Object> {

    private RedisSerializer jsonRedisSerializer = RedisSerializer.json();
    private RedisSerializer stringRedisSerializer = RedisSerializer.string();

    @Override
    public Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public byte[] serialize(Object source) throws SerializationException {
        if (source == null) return new byte[0];
        if (source instanceof JsonSerializer) {
            return jsonRedisSerializer.serialize(source);
        } else {
            return stringRedisSerializer.serialize(source);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return null;
    }
}
