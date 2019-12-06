package com.legooframework.model.hmdata.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class HttpPostAction {

    private static final Logger logger = LoggerFactory.getLogger(HttpPostAction.class);

    public String post(String postUrl, Map<String, Object> params, UUID batchNo) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("postUrl=%s[%s] postData...", postUrl, batchNo));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(postUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(_params)
                .retrieve().bodyToMono(String.class);
        String payload = mono.block(Duration.ofSeconds(timeout));
        Preconditions.checkState(!Strings.isNullOrEmpty(payload), "数据无返回，通信异常或者超时...");
        if (logger.isInfoEnabled())
            logger.info(String.format("postUrl=%s[%s] killTime=[%sms] receiveData=%s", postUrl, batchNo,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), payload));
        return payload;
    }

    @Value(value = "${request.timeout}")
    private int timeout;

}
