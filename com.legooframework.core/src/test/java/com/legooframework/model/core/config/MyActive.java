package com.legooframework.model.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ResourceUtils;

public class MyActive {
    private static final Logger logger = LoggerFactory.getLogger(MyActive.class);

    public void handle(@Payload String messae) {
        logger.debug(messae);
    }


    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statement/spring-int-cfg.xml"
        });
        MessagingTemplate messagingTemplate = app.getBean(MessagingTemplate.class);
        Message<String> msg = MessageBuilder.withPayload("nihao").build();
        messagingTemplate.send("channel_wx_send", msg);
    }
}
