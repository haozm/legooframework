package com.legooframework.model.httpproxy.entity;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.nutz.http.Http;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static org.junit.Assert.*;

public class HttpProxyEntityActionTest {

    @Test
    public void asd() {
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
        String response = mono.block(Duration.ofSeconds(60));
        System.out.println(response);
    }


}