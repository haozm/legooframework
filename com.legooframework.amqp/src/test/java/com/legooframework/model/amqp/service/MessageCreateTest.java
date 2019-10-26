package com.legooframework.model.amqp.service;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/amqp/spring-model-cfg.xml"}
)
public class MessageCreateTest {

    @Test
    public void sendDataToQueue() {
        for (int i = 0; i < 5000; i++) {
            Map<String, String> param = Maps.newHashMap();
            param.put("hao", String.format("xiaojie[%s]", i));
            messageCreate.sendDataToQueue("demoQueueKey", param);
        }
    }

    @Autowired
    private MessageCreate messageCreate;
}