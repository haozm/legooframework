package com.csosm.module.base.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.Map;

public class GuavaCacheManagerFactoryBean extends AbstractFactoryBean<GuavaCacheManager> {

    private static final Logger logger = LoggerFactory.getLogger(GuavaCacheManagerFactoryBean.class);

    @Override
    public Class<GuavaCacheManager> getObjectType() {
        return GuavaCacheManager.class;
    }

    @Override
    protected GuavaCacheManager createInstance() throws Exception {
        Map<String, GuavaCache> cacheMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(cacheCfg)) {
            for (Map.Entry<String, String> entry : cacheCfg.entrySet()) {
                CacheBuilder<Object, Object> builder =
                        CacheBuilder.from(CacheBuilderSpec.parse(entry.getValue()));
                cacheMap.put(
                        entry.getKey(), new GuavaCache(entry.getKey(), builder.build(), entry.getValue()));
                if (logger.isDebugEnabled())
                    logger.debug(
                            String.format("Init cache=%s from %s is OK.", entry.getKey(), entry.getValue()));
            }
        }
        return new GuavaCacheManager(cacheMap, defaultName);
    }

    private String defaultName;

    private Map<String, String> cacheCfg;

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public void setCacheCfg(Map<String, String> cacheCfg) {
        this.cacheCfg = cacheCfg;
    }

}
