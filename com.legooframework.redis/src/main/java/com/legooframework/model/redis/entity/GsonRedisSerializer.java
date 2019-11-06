package com.legooframework.model.redis.entity;

import com.google.common.base.Charsets;
import com.google.gson.*;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.StringJoiner;

public class GsonRedisSerializer implements RedisSerializer<Object> {

    private final static DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");
    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    private final Gson gson;
    final static byte[] PREFIX = new byte[]{0xF, 0xF};

    GsonRedisSerializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        builder.serializeNulls();
        this.gson = builder.create();
    }

    @Override
    public byte[] serialize(@Nullable Object source) throws SerializationException {
        if (source == null) {
            return new byte[0];
        }
        String clazz = source.getClass().getName();
        try {
            StringJoiner joiner = new StringJoiner("@_@");
            joiner.add(clazz).add(gson.toJson(source));
            return joiner.toString().getBytes(Charsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return null;
    }

    private static class LocalDateSerializer implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(YYYYMMDD));
        }

        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), YYYYMMDD);
        }
    }

    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(YYYYMMDDHHMMSS));
        }

        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), YYYYMMDDHHMMSS);
        }
    }
}
