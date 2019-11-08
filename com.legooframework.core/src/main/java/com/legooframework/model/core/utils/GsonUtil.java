package com.legooframework.model.core.utils;

import com.google.gson.*;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;

public abstract class GsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(GsonUtil.class);
    private final static DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");
    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static Gson gson;

    static {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .serializeNulls();
        gson = builder.create();
    }

    public static String serialize(@Nullable Object source) throws SerializationException {
        if (source == null) return null;
        try {
            return gson.toJson(source);
        } catch (Exception e) {
            logger.error(String.format("gson.toJson(%s) has exception", source), e);
            throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    public static <T> T deserialize(String source, Type typeOfT) throws SerializationException {
        try {
            return gson.fromJson(source, typeOfT);
        } catch (JsonSyntaxException e) {
            logger.error(String.format("gson.fromJson(%s,%s) has exception", source, typeOfT), e);
            throw new SerializationException(String.format("Not found Type %s", source));
        }

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
