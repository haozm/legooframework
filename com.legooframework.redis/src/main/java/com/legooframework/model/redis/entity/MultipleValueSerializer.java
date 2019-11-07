package com.legooframework.model.redis.entity;

import com.legooframework.model.core.base.entity.GsonSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Collection;
import java.util.Optional;

public class MultipleValueSerializer implements RedisSerializer<Object> {

    private RedisSerializer stringRedisSerializer = RedisSerializer.string();
    private RedisSerializer javaRedisSerializer = RedisSerializer.java();

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
        } else if (source instanceof Collection) {
            if (CollectionUtils.isEmpty((Collection<?>) source)) return new byte[0];
            Object next = ((Collection<?>) source).iterator().next();
            if (next instanceof GsonSerializer) {

            }
        } else {
            return stringRedisSerializer.serialize(source.toString());
        }
        return new byte[0];
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
