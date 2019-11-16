package com.legooframework.model.httpproxy.entity;

import com.legooframework.model.core.base.entity.EmptyEntity;
import com.legooframework.model.core.base.entity.HttpBaseEntityAction;
import com.legooframework.model.httpproxy.service.HttpProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class HttpProxyEntityAction extends HttpBaseEntityAction<EmptyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    public HttpProxyEntityAction() {
    }

    public String postJsonTarget(HttpGateWayParams gateWayParams, Object params) {
        Mono<String> mono;
        if (null != params) {
            mono = WebClient.create().method(HttpMethod.POST)
                    .uri(gateWayParams.getTarget())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve().bodyToMono(String.class);
        } else {
            mono = WebClient.create().method(HttpMethod.POST)
                    .uri(gateWayParams.getTarget())
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(String.class);
        }
        //IllegalStateException
        try {
            return mono.block(Duration.ofSeconds(gateWayParams.getTimeout()));
        } catch (IllegalStateException e) {
            logger.error(String.format("Post %s is Timeout %d By Fusing", gateWayParams.getTarget(), gateWayParams.getTimeout()));
            throw new FusingTimeOutException(gateWayParams);
        }
    }

}
