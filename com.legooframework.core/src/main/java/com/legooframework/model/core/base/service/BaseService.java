package com.legooframework.model.core.base.service;

import com.legooframework.model.core.event.MessageGateWay;
import com.legooframework.model.core.osgi.Bundle;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public abstract class BaseService implements ApplicationContextAware {

    protected <T> T getBean(Class<T> clazz) {
        return BeanFactoryUtils.beanOfType(this.appCtx, clazz);
    }

    protected <T> T getBean(String beanName, Class<T> clazz) {
        return this.appCtx.getBean(beanName, clazz);
    }

    //  获取一组实现接口的 Spring Bean
    protected <T> Optional<Map<String, T>> getBeanMap(Class<T> clazz) {
        Map<String, T> clazzMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.appCtx, clazz);
        return Optional.ofNullable(MapUtils.isEmpty(clazzMap) ? null : clazzMap);
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    protected <T> Optional<Collection<T>> getBeanList(Class<T> clazz) {
        Optional<Map<String, T>> clazzMap = getBeanMap(clazz);
        return clazzMap.map(Map::values);
    }

    protected abstract Bundle getLocalBundle();

    protected MessageGateWay getEventBus() {
        return getBean(MessageGateWay.class);
    }

    protected ThreadPoolTaskExecutor getExecutor() {
        return getBean("legoo-executor", ThreadPoolTaskExecutor.class);
    }

    protected MessageGateWay getAsycEventBus() {
        return getBean(MessageGateWay.class);
    }

    private ApplicationContext appCtx;
}
