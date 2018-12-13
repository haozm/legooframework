package com.legooframework.model.membercare.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

public class TestMyJobStepTest {

    public static void main(String[] args) throws Exception {
        ApplicationContext appCtx = new ClassPathXmlApplicationContext(
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml");
    }

}