package com.legooframework.model.crmadapter.entity;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class TenantsRouteFactoryTest {
//    ReactorClientHttpConnector
    @Test
    public void getUrl() {
        System.out.println(tenantsRoute.getUrl(1, "loadAllCompany"));
    }

    @Test
    public void getWebClient() {
        Map<String, Object> user = Maps.newHashMap();
        user.put("type", "company");
        Mono<String> payload = WebClient.create().method(HttpMethod.POST)
                .uri("http://test00.csosm.com:9001/api/orgmvc/inner/all.json?loginId=-1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .syncBody(user)
                .retrieve().bodyToMono(String.class);
        String res = payload.block();
        System.out.println(res);
    }

    @Autowired
    TenantsRouteFactory tenantsRoute;
}