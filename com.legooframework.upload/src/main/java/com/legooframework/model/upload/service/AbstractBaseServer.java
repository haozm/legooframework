package com.legooframework.model.upload.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class AbstractBaseServer implements ApplicationContextAware {

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

    private ApplicationContext appCtx;


    public void setApplicationContext(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }
}
