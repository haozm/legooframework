package com.legooframework.model.core.web;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

import java.util.Map;

public class JsonMessage {
    private String code;
    private String msg;
    private String errDetail;
    private Map<String, Object> header;
    private Object data;

    JsonMessage(String code, String message, String errDetail,
                Map<String, Object> header, Object payload) {
        this.code = code;
        this.msg = message;
        this.errDetail = errDetail;
        this.header = Maps.newHashMap(header);
        this.data = payload;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return msg;
    }

    public String getErrDetail() {
        return errDetail;
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public Object getData() {
        return data;
    }

    public Map<String, Object> toImMsg() {
        Map<String, Object> msg = Maps.newHashMap();
        msg.put("code", "0000".equals(this.code) ? "0" : code);
        msg.put("message", this.getMessage());
        msg.put("data", data);
        return msg;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("msg", msg)
                .add("errDetail", errDetail)
                .toString();
    }
}
