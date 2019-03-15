package com.legooframework.model.dict.service;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.event.MessageGateWay;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.dict.event.DictEventFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

public class EventListenerServiceTest {

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext(
                        ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/juniit/spring-db-cfg.xml",
                        ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                        ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                        ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/dict/spring-model-cfg.xml");
        LoginContext context = LoginContextHolder.getAnonymousCtx(100000000L);
        LoginContextHolder.setCtx(context);
        MessageGateWay messageGateWay = applicationContext.getBean(MessageGateWay.class);
        Bundle bundle = applicationContext.getBean("dictBundle", Bundle.class);
        LegooEvent event = DictEventFactory.loadDictByTypeEvent(bundle, "SEX");
        Object message = messageGateWay.sendAndReceive(event, Object.class);
        System.out.println(message);
    }
}