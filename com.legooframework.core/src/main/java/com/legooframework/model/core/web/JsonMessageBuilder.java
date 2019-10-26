package com.legooframework.model.core.web;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.exception.BaseException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class JsonMessageBuilder {

    private String code;
    private String message;
    private String errDetail;
    private Map<String, Object> header;
    private Object payload;

    private static String OK_CODE = "0000";
    private static String WARN_CODE = "1";

    private JsonMessageBuilder(String code, String message, String errDetail) {
        Preconditions.checkArgument(StringUtils.isNoneEmpty(code));
        this.code = code;
        header = Maps.newHashMap();
        this.errDetail = errDetail;
        this.message = message;
        if (OK_CODE.equals(code))
            this.message = "success";
    }

    public static JsonMessageBuilder OK() {
        return new JsonMessageBuilder(OK_CODE, null, null);
    }

    public static JsonMessageBuilder WARN(String message) {
        return new JsonMessageBuilder(WARN_CODE, message, null);
    }

    public static JsonMessageBuilder ERROR(Exception exception) {
        Preconditions.checkNotNull(exception);
        if (exception instanceof BaseException) {
            BaseException be = (BaseException) exception;
            String detail = be.getOriginal().isPresent() ? be.getOriginal().get().getMessage() : null;
            return new JsonMessageBuilder(be.getErrCode(), be.getMessage(), detail);
        } else if (exception instanceof RuntimeException) {
            RuntimeException re = (RuntimeException) exception;
            return new JsonMessageBuilder("9998", re.getMessage(), re.getMessage());
        } else {
            return new JsonMessageBuilder("9999", "未知错误", exception.getMessage());
        }
    }

    public static JsonMessageBuilder ERROR(String errmsg) {
        return new JsonMessageBuilder("9999", errmsg, errmsg);
    }

    public static JsonMessageBuilder ERROR(String errCode, String errmsg) {
        return new JsonMessageBuilder(errCode, errmsg, errmsg);
    }

    public static JsonMessageBuilder NOTLOGIN(String errmsg) {
        return new JsonMessageBuilder("4004", errmsg, errmsg);
    }

    public JsonMessageBuilder withHeader(Map<String, Object> header) {
        if (MapUtils.isNotEmpty(header))
            this.header.putAll(header);
        return this;
    }

    public JsonMessageBuilder withHeader(String key, Object value) {
        Preconditions.checkArgument(StringUtils.isNoneEmpty(key));
        this.header.put(key, value);
        return this;
    }

    public JsonMessageBuilder withPayload(Object payload) {
        if (payload instanceof Optional) {
            this.payload = ((Optional<?>) payload).orElse(null);
        } else {
            this.payload = payload;
        }
        return this;
    }

    public JsonMessage toMessage() {
        return new JsonMessage(code, message, errDetail, header, payload);
    }

}
