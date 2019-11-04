package com.legooframework.model.redis.entity;

import com.google.gson.*;

import java.lang.reflect.Type;

public class AsdSerializer implements JsonSerializer<Asd>, JsonDeserializer<Asd> {

    @Override
    public JsonElement serialize(Asd src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

    @Override
    public Asd deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
