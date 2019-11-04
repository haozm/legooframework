package com.legooframework.model.redis.entity;

import com.google.common.base.Charsets;
import com.google.gson.*;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;

public class GsonRedisSerializer implements RedisSerializer<Object> {

    private final Gson gson;

    public GsonRedisSerializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        builder.serializeNulls();
        this.gson = builder.create();
    }

    private static class LocalDateSerializer implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(DateTimeUtils.YYYYMMDD));
        }

        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return DateTimeUtils.parseShortYYYYMMDD(json.getAsJsonPrimitive().getAsString());
        }
    }

    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(DateTimeUtils.YYYYMMDDHHMMSS));
        }

        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return DateTimeUtils.parseYYYYMMDDHHMMSS(json.getAsJsonPrimitive().getAsString());
        }
    }


    @Override
    public byte[] serialize(@Nullable Object source) throws SerializationException {
        if (source == null) {
            return new byte[0];
        }
        String className = source.getClass().getName();
        try {
            return gson.toJson(source).getBytes(Charsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return null;
    }
}
