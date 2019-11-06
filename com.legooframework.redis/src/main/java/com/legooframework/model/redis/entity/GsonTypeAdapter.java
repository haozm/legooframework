package com.legooframework.model.redis.entity;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface GsonTypeAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {

    Class<T> getType();
}
