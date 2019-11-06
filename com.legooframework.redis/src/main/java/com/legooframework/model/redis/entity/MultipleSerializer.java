package com.legooframework.model.redis.entity;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Optional;

public class MultipleSerializer implements RedisSerializer<Object> {

    private RedisSerializer jsonRedisSerializer = RedisSerializer.json();
    private RedisSerializer stringRedisSerializer = RedisSerializer.string();
    private RedisSerializer gsonRedisSerializer = new GsonRedisSerializer();

    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(Object source) throws SerializationException {
        if (source == null) {
            return new byte[0];
        }
        if (source instanceof GsonSerializer) {
            return gsonRedisSerializer.serialize(source);
        } else {
            return stringRedisSerializer.serialize(source);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        Optional<byte[]> source = GsonRedisSerializer.hasGsonSerializer(bytes);
        return source.map(value -> gsonRedisSerializer.deserialize(value)).orElse(null);
    }
}
