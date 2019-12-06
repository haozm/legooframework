package com.legooframework.model.hmdata.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class GsonUtil {

    private static Type MAP_TYPETOKEN = new TypeToken<Map<String, String>>() {
    }.getType();
    private final static Gson gson = new Gson();

    public static Map<String, String> fromJsonByMap(String data) {
        return gson.fromJson(data, MAP_TYPETOKEN);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

}
