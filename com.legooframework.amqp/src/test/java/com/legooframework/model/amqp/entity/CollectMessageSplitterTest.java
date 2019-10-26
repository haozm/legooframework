package com.legooframework.model.amqp.entity;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/amqp/spring-model-ins.xml"}
)
public class CollectMessageSplitterTest {

    @Test
    public void splitMessage() {
        List<String> list = Lists.newArrayList("hao", "xiaojie", "qiu", "fu", "123");
        Message<List<String>> message = MessageBuilder.withPayload(list).build();
        messagingTemplate.send("directChannel", message);
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/amqp/spring-model-ins.xml");
        MessagingTemplate messagingTemplate = applicationContext.getBean("messagingTemplate", MessagingTemplate.class);
        List<String> list = Lists.newArrayList("hao", "xiaojie", "qiu", "fu", "123");
        Message<List<String>> message = MessageBuilder.withPayload(list).build();
        messagingTemplate.send("directChannel", message);
    }

    @Autowired
    private MessagingTemplate messagingTemplate;

}