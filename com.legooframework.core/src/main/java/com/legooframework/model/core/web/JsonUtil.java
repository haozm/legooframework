package com.legooframework.model.core.web;

import com.google.gson.*;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

public abstract class JsonUtil {

    private final static DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");
    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static Gson gson;

    static {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        gson = builder.create();
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
