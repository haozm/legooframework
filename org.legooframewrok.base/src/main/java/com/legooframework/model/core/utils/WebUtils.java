package com.legooframework.model.core.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class WebUtils extends org.springframework.web.util.WebUtils {

    private final static Gson gson = new Gson();
    private static Type MAP_TYPETOKEN = new TypeToken<Map<String, String>>() {
    }.getType();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static Map<String, String> fromJsonByMap(String data) {
        return gson.fromJson(data, MAP_TYPETOKEN);
    }

}
