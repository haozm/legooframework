package com.legooframework.model.httpproxy.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.http.inbound.HttpRequestHandlingEndpointSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/httpproxy/spring-model-cfg.xml"}
)
public class HttpGateWayFactoryBeanTest {
//    HttpRequestHandlingEndpointSupport
//    PublishSubscribeChannel
    @Test
    public void createInstance() {
        System.out.println(httpGateWayFactory == null);
    }

    @Autowired
    private HttpGateWayFactory httpGateWayFactory;
}