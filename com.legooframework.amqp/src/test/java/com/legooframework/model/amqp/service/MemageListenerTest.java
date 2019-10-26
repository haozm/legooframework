package com.legooframework.model.amqp.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

public class MemageListenerTest {


    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/amqp/spring-model-cfg.xml");
    }
}