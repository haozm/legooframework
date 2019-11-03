package com.legooframework.model.redis.entity;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

public class RedisKeyEventMessageListenerTest {

    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/redis/spring-model-cfg.xml");
    }
}