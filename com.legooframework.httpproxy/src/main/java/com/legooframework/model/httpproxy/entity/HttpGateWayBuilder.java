package com.legooframework.model.httpproxy.entity;

import com.google.common.base.MoreObjects;

public class HttpGateWayBuilder {

    private final String id, antPath;
    private String target;

    public HttpGateWayBuilder(String id, String antPath) {
        this.id = id;
        this.antPath = antPath;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public HttpGateWayEntity build() {
        return new HttpGateWayEntity(id, antPath, target);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("antPath", antPath)
                .add("target", target)
                .toString();
    }
}
