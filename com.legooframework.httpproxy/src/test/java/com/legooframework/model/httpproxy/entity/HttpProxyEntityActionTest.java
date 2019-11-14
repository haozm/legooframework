package com.legooframework.model.httpproxy.entity;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

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

    @Test
    public void riComponentsBuilder() {
        String httpUrl = "http://testold.csosm.com/httpproxy/api/statistical/layout/load/homepage.json?hao=xiaojie&asd=asdasd";
        //String httpUrl = "http://testold.csosm.com/httpproxy/api/stati234l/layout/load/homepage.json?hao=xiaojie&asd=asdasd&mod=haox";
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(httpUrl).build();
        System.out.println(uri.getHost());
        System.out.println(uri.getPath());
        System.out.println(uri.getFragment());
        System.out.println(uri.getScheme());
        System.out.println(uri.getQuery());
        System.out.println(uri.getPathSegments());
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        System.out.println(antPathMatcher.match("/*/api/statistical/**", uri.getPath()));
        UriComponents nse = UriComponentsBuilder.newInstance().host(uri.getHost()).port(uri.getPort()).scheme(uri.getScheme())
                .pathSegment("hao", "asd", "jie").path("asdasdasd/asdasd/asd/asd.sda").query(uri.getQuery()).build();
        System.out.println(nse);
    }


}