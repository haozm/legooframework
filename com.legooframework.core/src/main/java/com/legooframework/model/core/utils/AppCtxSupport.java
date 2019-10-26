package com.legooframework.model.core.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class AppCtxSupport implements ApplicationContextAware {

    protected <T> T getBean(String beanId, Class<T> classes) {
        try {
            return this.appCtx.getBean(beanId, classes);
        } catch (BeansException e) {
            throw new RuntimeException(String.format("尚未配置 %s mapping %s 对应的Bean", beanId, classes));
        }
    }

    protected <T> T getBean(Class<T> classes) {
        try {
            return this.appCtx.getBean(classes);
        } catch (BeansException e) {
            throw new RuntimeException(String.format("尚未配置 %s 对应的Bean", classes));
        }
    }

    private ApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }
}
