package com.legooframework.model.httpproxy.service;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

public class HttpProxyService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    /**
     * @param message OXXO
     * @return OOXX
     */
    public Object postProxy(Message<?> message) {
        MessageHeaderAccessor headerAccessor = MessageHeaderAccessor.getMutableAccessor(message);
        if (logger.isDebugEnabled())
            logger.debug(String.format("postProxy(host=%s, url=%s, contentType=%s, requestMethod=%s) and payload = %s",
                    headerAccessor.getHeader("host"), headerAccessor.getHeader("http_requestUrl"),
                    headerAccessor.getHeader("contentType"),
                    headerAccessor.getHeader("http_requestMethod"), message.getPayload()));
        return test();
    }

    public String test() {
        String httpUrl = "http://testold.csosm.com/statistical/api/layout/load/homepage.json";
        Map<String, Object> params = Maps.newHashMap();
        params.put("int_search_companyId", 1);
        params.put("int_search_userId", 132);
        params.put("str_pageType", "EMPLOYEE_SALES01");
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(httpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve().bodyToMono(String.class);
        return mono.block(Duration.ofSeconds(60));
    }

    private void authentication() {

    }

}
