package com.csosm.commons.util;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.WebUtils;

import com.csosm.commons.mvc.HttpMessage;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class MyWebUtil extends WebUtils {

    private static final Logger logger = LoggerFactory.getLogger(MyWebUtil.class);

    private static Gson gson = new Gson();
    private static Splitter splitter = Splitter.on("@###@");

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    public static List<String> splitSoftInfo(String msg) {
        if (Strings.isNullOrEmpty(msg)) return Collections.EMPTY_LIST;
        return splitter.splitToList(msg);
    }

    // {"code":"9998","msg":"pinCode \u003d 216014 已经被激活使用...","errDetail":"pinCode \u003d 216014 已经被激活使用...","header":{}}
    public static HttpMessage decode4HttpMessage(String json_data) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(json_data), "待解析的报文不可以为空值...");
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(json_data).getAsJsonObject();
            String code = getString(jsonObject, "code");
            String msg = getString(jsonObject, "msg");
            String errDetail = getString(jsonObject, "errDetail");
            return new HttpMessage(code, msg, errDetail);
        } catch (Exception e) {
            logger.error(String.format("decode msg [%s] from upd has error", json_data), e);
            throw new RuntimeException(String.format("decode 4 HttpMessage msg [%s] from upd has error", json_data));
        }
    }

    public static String getString(JsonObject jsonObject, String property) {
        JsonElement jsonElement = jsonObject.get(property);
        if (jsonElement == null || jsonElement.isJsonNull()) return null;
        return jsonElement.getAsString();
    }

    public static String toJson(Object value) {
        return gson.toJson(value);
    }
}
