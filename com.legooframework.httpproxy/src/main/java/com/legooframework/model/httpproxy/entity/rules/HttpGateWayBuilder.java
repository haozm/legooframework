package com.legooframework.model.httpproxy.entity.rules;

public class HttpGateWayBuilder {

    private final String id, antPath;
    private String target;

    HttpGateWayBuilder(String id, String antPath) {
        this.id = id;
        this.antPath = antPath;
    }

    void setTarget(String target) {
        this.target = target;
    }


    void build() {

    }
}
