package com.legooframework.model.httpproxy.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

public class HttpGateWayEntity extends BaseEntity<String> implements IGateWay {

    private final String[] params;
    private final String domain, path;
    private final int fuseCount;

    HttpGateWayEntity(String id, String[] params, String domain, String path, int fuseCount) {
        super(id);
        this.params = params;
        this.domain = domain;
        this.path = path;
        this.fuseCount = fuseCount;
    }

    @Override
    public boolean match(UriComponents originalUri) {
        return StringUtils.equalsAnyIgnoreCase(params[1], originalUri.getQueryParams().getFirst(params[0]));
    }

    @Override
    public String getTatget(UriComponents originalUri) {
        String subPath = StringUtils.substringAfter(originalUri.getPath(), "/api/");
        return UriComponentsBuilder.fromHttpUrl(domain).path(this.path)
                .path(subPath).query(originalUri.getQuery()).toUriString();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("params", Arrays.toString(params))
                .add("domain", domain)
                .add("path", path)
                .add("fuseCount", fuseCount)
                .toString();
    }
}
