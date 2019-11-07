package com.legooframework.model.redis.entity;

import org.springframework.data.redis.serializer.RedisElementReader;
import org.springframework.data.redis.serializer.RedisElementWriter;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.nio.ByteBuffer;

public class DateTimeSerializationPair implements RedisSerializationContext.SerializationPair {

    @Override
    public RedisElementReader getReader() {
        return null;
    }

    @Override
    public RedisElementWriter getWriter() {
        return null;
    }

    @Override
    public Object read(ByteBuffer buffer) {
        return null;
    }

    @Override
    public ByteBuffer write(Object element) {
        return null;
    }
}
