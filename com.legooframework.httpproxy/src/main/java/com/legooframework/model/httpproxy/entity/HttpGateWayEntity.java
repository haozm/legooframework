package com.legooframework.model.httpproxy.entity;

import com.legooframework.model.core.base.entity.BaseEntity;
import org.springframework.util.AntPathMatcher;

public class HttpGateWayEntity extends BaseEntity<String> {

    private final AntPathMatcher antPathMatcher;
    private final String target;

    public HttpGateWayEntity(String id, String pattern, String target) {
        super(id);
        this.antPathMatcher = new AntPathMatcher(pattern);
        this.target = target;
    }
}
