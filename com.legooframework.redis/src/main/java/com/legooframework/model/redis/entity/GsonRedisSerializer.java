package com.legooframework.model.redis.entity;

import com.google.common.base.Charsets;
import com.google.gson.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class GsonRedisSerializer implements RedisSerializer<Object>, InitializingBean {

    private final static DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");
    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    private final GsonBuilder builder;
    private Gson gson;
    private final static String PREFIX = "GSON";
    private final static String PREFIX_CHAR = "GSON@";
    private final static byte[] PREFIX_BYTES = PREFIX_CHAR.getBytes(Charsets.UTF_8);

    private List<GsonTypeAdapter> typeAdapters;

    public void setTypeAdapters(List<GsonTypeAdapter> typeAdapters) {
        this.typeAdapters = typeAdapters;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(typeAdapters)) {
            typeAdapters.forEach($it -> this.builder.registerTypeAdapter($it.getType(), $it));
        }
        this.gson = builder.create();
    }

    GsonRedisSerializer() {
        this.builder = new GsonBuilder();
        GsonTypeAdapter localDateSerializer = new LocalDateSerializer();
        GsonTypeAdapter localDateTimeSerializer = new LocalDateTimeSerializer();
        this.builder.registerTypeAdapter(localDateSerializer.getType(), localDateSerializer);
        this.builder.registerTypeAdapter(localDateTimeSerializer.getType(), new LocalDateTimeSerializer());
        this.builder.serializeNulls();

    }

    @Override
    public byte[] serialize(@Nullable Object source) throws SerializationException {
        if (source == null) return new byte[0];
        String clazz = source.getClass().getName();
        try {
            StringJoiner joiner = new StringJoiner("@");
            joiner.add(PREFIX).add(clazz).add(gson.toJson(source));
            return joiner.toString().getBytes(Charsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        String source = new String(bytes, Charsets.UTF_8);
        String[] args = StringUtils.split(source, "@", 2);
        try {
            Class<?> clazz = Class.forName(args[0]);
            return gson.fromJson(args[1], clazz);
        } catch (ClassNotFoundException e) {
            throw new SerializationException(String.format("Not found Class %s", args[0]));
        }
    }

    static Optional<byte[]> hasGsonSerializer(byte[] source) {
        final int length = PREFIX_BYTES.length;
        final int source_len = source.length;
        byte[] prefix_args = new byte[length];
        System.arraycopy(source, 0, prefix_args, 0, length);
        if (Arrays.equals(PREFIX_BYTES, prefix_args)) {
            return Optional.of(ArrayUtils.subarray(source, length, source_len));
        }
        return Optional.empty();
    }

    private static class LocalDateSerializer implements GsonTypeAdapter<LocalDate> {

        @Override
        public Class<LocalDate> getType() {
            return LocalDate.class;
        }

        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(YYYYMMDD));
        }

        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), YYYYMMDD);
        }
    }

    private static class LocalDateTimeSerializer implements GsonTypeAdapter<LocalDateTime> {

        @Override
        public Class<LocalDateTime> getType() {
            return LocalDateTime.class;
        }

        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(YYYYMMDDHHMMSS));
        }

        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), YYYYMMDDHHMMSS);
        }
    }
}
