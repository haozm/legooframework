package com.legooframework.model.core.osgi;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class BundleRuntimeFactory implements InitializingBean, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BundleRuntimeFactory.class);

    private final Map<String, Bundle> bundleMap;

    public Collection<Bundle> getBundles() {
        return bundleMap.values();
    }

    public BundleRuntimeFactory() {
        this.bundleMap = Maps.newConcurrentMap();
    }

    public Collection<String> getBundleNames() {
        return bundleMap.values().stream().map(Bundle::getName).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Bundle> bundleMap = appCtx.getBeansOfType(Bundle.class);
        if (MapUtils.isNotEmpty(bundleMap))
            bundleMap.values().forEach(x -> this.bundleMap.put(x.getName(), x));
        if (logger.isInfoEnabled() && MapUtils.isNotEmpty(bundleMap))
            this.bundleMap.values().forEach(x -> logger.info(String.format("Registered Module is %s", x.getName())));
    }

    private ApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

}
