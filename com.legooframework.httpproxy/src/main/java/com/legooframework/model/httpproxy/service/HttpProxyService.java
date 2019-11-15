package com.legooframework.model.httpproxy.service;

import com.legooframework.model.httpproxy.entity.HttpGateWayParams;
import com.legooframework.model.httpproxy.entity.HttpRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class HttpProxyService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    /**
     * @param message OXXO
     * @return OOXX
     */
    public Object postProxy(Message<?> message) {
        HttpRequestDto requestDto = new HttpRequestDto(message);
        if (logger.isDebugEnabled())
            logger.debug(requestDto.toString());
        HttpGateWayParams gateWayParams = getHttpGateWayFactory().getTarget(requestDto);
        return postTarget(gateWayParams, requestDto.getBody().orElse(null));
    }

    private String postTarget(HttpGateWayParams gateWayParams, Object params) {
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
        return mono.block(Duration.ofSeconds(gateWayParams.getTimeout()));
    }

}
