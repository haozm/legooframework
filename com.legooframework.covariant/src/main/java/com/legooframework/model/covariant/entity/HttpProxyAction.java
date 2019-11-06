package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public abstract class HttpProxyAction<T extends BaseEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyAction.class);
    final static String KEY_HEADER = "Authorization";

    private Map<String, String> urlMapping;

    HttpProxyAction() {
        this.urlMapping = Maps.newHashMap();
    }

    public void setUrlMapping(Map<String, String> urlMapping) {
        if (MapUtils.isNotEmpty(urlMapping)) this.urlMapping.putAll(urlMapping);
    }

    /**
     * 网络请求 解析返回值
     *
     * @param action 请求地址
     * @return 我的太阳
     */
    Optional<JsonElement> postAction(String action, String token, Map<String, Object> params, Object... pathVariables) {
        String postUrl = this.urlMapping.get(action);
        if (logger.isTraceEnabled())
            logger.trace(String.format("URL=%s,params,size =%s pathVariables=%s,token=%s", postUrl,
                    MapUtils.isEmpty(params) ? 0 : params.size(), Arrays.toString(pathVariables), token));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        _params.put("bundle", "com.legooframework.RectProxy");
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(postUrl, pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpProxyAction.KEY_HEADER, token)
                .bodyValue(_params)
                .retrieve().bodyToMono(String.class);
        String payload = mono.block();
        Preconditions.checkNotNull(payload, "数据无返回，通信异常...");
        if (logger.isTraceEnabled())
            logger.trace(String.format("URL=%s,params=%s,return %s", postUrl, MapUtils.isEmpty(params) ? 0 : params.size(),
                    payload.length()));
        return WebUtils.parseJson(payload);
    }

}
