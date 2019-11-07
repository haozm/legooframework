package com.legooframework.model.redis.entity;

import com.legooframework.model.core.base.entity.GsonSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Optional;

public class MultipleSerializer implements RedisSerializer<Object> {

    private RedisSerializer stringRedisSerializer = RedisSerializer.string();

    public void setGsonRedisSerializer(RedisSerializer gsonRedisSerializer) {
        this.gsonRedisSerializer = gsonRedisSerializer;
    }

    private RedisSerializer gsonRedisSerializer;

    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(Object source) throws SerializationException {
        if (source == null) return new byte[0];
        if (source instanceof GsonSerializer) {
            return gsonRedisSerializer.serialize(source);
        } else {
            return stringRedisSerializer.serialize(source.toString());
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if ((bytes == null || bytes.length == 0)) return null;
        Optional<byte[]> source = GsonRedisSerializer.hasGsonSerializer(bytes);
        if (source.isPresent()) {
            return gsonRedisSerializer.deserialize(source.get());
        } else {
            return stringRedisSerializer.deserialize(bytes);
        }
    }
}
