package com.csosm.commons.server;

import com.csosm.commons.event.EventBusSubscribe;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class EventRegisterServer implements InitializingBean, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventRegisterServer.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventBusSubscribe> subscribeMap = this.appCtx.getBeansOfType(EventBusSubscribe.class);
        if (MapUtils.isNotEmpty(subscribeMap))
            for (EventBusSubscribe $it : subscribeMap.values()) {
                if (logger.isInfoEnabled())
                    logger.info(String.format("register %s to Event And AsyncEventBus botch", $it.getClass()));
                getAsyncEventBus().register($it);
                getEventBus().register($it);
            }
        getAsyncEventBus().register(new DeadEventSub());
        getEventBus().register(new DeadEventSub());
    }

    AsyncEventBus getAsyncEventBus() {
        return appCtx.getBean("csosmAsyncEventBus", AsyncEventBus.class);
    }

    EventBus getEventBus() {
        return appCtx.getBean("csosmEventbus", EventBus.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    private ApplicationContext appCtx;
}
