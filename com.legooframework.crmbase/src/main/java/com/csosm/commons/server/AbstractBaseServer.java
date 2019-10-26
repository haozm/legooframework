package com.csosm.commons.server;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.BusEvent;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractBaseServer implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseServer.class);

    protected <T> T getBean(Class<T> clazz) {
        return appCtx.getBean(clazz);
    }

    protected <T> T getBean(String beanId, Class<T> clazz) {
        return appCtx.getBean(beanId, clazz);
    }

    protected Object getBean(String className) {
        return appCtx.getBean(className);
    }

    protected ApplicationContext getAppCtx() {
        return appCtx;
    }

    protected AsyncEventBus getAsyncEventBus() {
        return appCtx.getBean("csosmAsyncEventBus", AsyncEventBus.class);
    }

    protected EventBus getEventBus() {
        return appCtx.getBean("csosmEventbus", EventBus.class);
    }

    private ApplicationContext appCtx;

    protected void logProxy(BusEvent event) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof LoginUserContext) event.setLoginUser((LoginUserContext) principal);
            getAsyncEventBus().post(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }
}
