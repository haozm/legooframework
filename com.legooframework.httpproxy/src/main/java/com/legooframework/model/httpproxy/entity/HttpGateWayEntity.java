package com.legooframework.model.httpproxy.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.springframework.util.AntPathMatcher;

public class HttpGateWayEntity extends BaseEntity<String> implements IGateWay {

    private final AntPathMatcher antPathMatcher;
    private final String target;

    public HttpGateWayEntity(String id, String pattern, String target) {
        super(id);
        this.antPathMatcher = new AntPathMatcher(pattern);
        this.target = target;
    }


    @Override
    public boolean match() {
        return false;
    }

    @Override
    public String getTatget() {
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("antPathMatcher", antPathMatcher)
                .add("target", target)
                .toString();
    }
}
