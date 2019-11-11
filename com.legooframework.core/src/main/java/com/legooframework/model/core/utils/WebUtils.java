package com.legooframework.model.core.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public abstract class WebUtils extends org.springframework.web.util.WebUtils {

    private final static Gson gson = new Gson();
    private final static String ATT_CODE = "code";
    private final static String VAL_SUCC = "0000";
    private final static String ATT_PALLOAD = "data";
    private final static String ATT_MSG = "msg";
    private final static URLCodec URL_CODEC = new URLCodec(Charsets.UTF_8.toString());
    public static String SECURE_ANONYMOUS_TOKEN = "eyJ1dWlkIjoiODQ3MzM1ODgtZDEyNy00NmYwLTg3NTAtNzYzODI5NWI1MGQ0IiwibG9naW5OYW0iOiIxMDAwOThAbmV3bXRqIiwiaG9zdCI6InVua293bl9ob3N0IiwibG9naW5UaW1lIjoiMjAxOTA1MjcxNDA4NDciLCJjaGFubmVsIjoxfQ==";
    private static Type MAP_TYPETOKEN = new TypeToken<Map<String, String>>() {
    }.getType();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static Map<String, String> fromJsonByMap(String data) {
        return gson.fromJson(data, MAP_TYPETOKEN);
    }

    public static Optional<JsonElement> parseJson(String jsonString) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jsonString), "入参 String jsonString 不可为空值.... ");
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String code = jsonObject.get(ATT_CODE).getAsString();
        if (!StringUtils.equals(VAL_SUCC, code)) {
            throw new RuntimeException(String.format("ERROE:%s , MSG:%s ", code, jsonObject.get(ATT_MSG).getAsString()));
        }
        return Optional.ofNullable((jsonObject.get(ATT_PALLOAD) == null || jsonObject.get(ATT_PALLOAD).isJsonNull()) ?
                null : jsonObject.get(ATT_PALLOAD));
    }

    public static String encodeUrl(String str) {
        try {
            return URL_CODEC.encode(str);
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decodeUrl(String str) {
        try {
            return URL_CODEC.decode(str);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

}
