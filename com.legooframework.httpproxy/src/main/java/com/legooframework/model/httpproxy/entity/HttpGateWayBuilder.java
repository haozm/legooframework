package com.legooframework.model.httpproxy.entity;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class HttpGateWayBuilder {

    private String id, domain, path;
    private final String[] params;
    private final int fuseCount, timeout;

    public HttpGateWayBuilder(String id, String params, int fuseCount, int timeout) {
        this.id = id;
        this.params = StringUtils.split(params, '=');
        this.fuseCount = fuseCount;
        this.timeout = timeout;
    }

    public void setPathInfo(String domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    public HttpGateWayEntity build() {
        return new HttpGateWayEntity(id, params, domain, path, fuseCount, timeout);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("params", Arrays.toString(params))
                .add("domain", domain)
                .add("path", path)
                .add("fuseCount", fuseCount)
                .toString();
    }
}
