package com.legooframework.model.httpproxy.entity;

public class HttpGateWayParams {

    private final String target;
    private final int timeout;

    HttpGateWayParams(String target, int timeout) {
        this.target = target;
        this.timeout = timeout;
    }

    String getTarget() {
        return target;
    }

    int getTimeout() {
        return timeout;
    }
}
