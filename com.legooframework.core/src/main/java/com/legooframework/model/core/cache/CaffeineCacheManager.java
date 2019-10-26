package com.legooframework.model.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;

public class CaffeineCacheManager implements CacheManager, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(CaffeineCacheManager.class);

    private Map<String, String> caffeineSpecs;
    private final Map<String, CaffeineCache> caffeineCache;

    public CaffeineCacheManager() {
        this.caffeineCache = Maps.newConcurrentMap();
    }

    public boolean containsCache(String cacheName) {
        return caffeineCache.containsKey(cacheName);
    }

    @Override
    @Nullable
    public Cache getCache(String cacheName) {
        if (!caffeineCache.containsKey(cacheName))
            logger.warn(String.format("Not exits cache which name is %s", cacheName));
        return caffeineCache.get(cacheName);
    }

    public void clearAll() {
        this.caffeineCache.values().forEach(CaffeineCache::clear);
        if (logger.isDebugEnabled())
            logger.debug(String.format("clearAll(%s) is finished", caffeineCache));
    }

    public void clearByCache(String cacheName) {
        if (this.caffeineCache.containsKey(cacheName)) {
            this.caffeineCache.get(cacheName).clear();
            if (logger.isDebugEnabled())
                logger.debug(String.format("clearByCache(%s) is finished", cacheName));
        }
    }


    @Override
    public Collection<String> getCacheNames() {
        return caffeineCache.keySet();
    }

    //    initialCapacity=[integer]: sets Caffeine.initialCapacity.
//    maximumSize=[long]: sets Caffeine.maximumSize.
//    maximumWeight=[long]: sets Caffeine.maximumWeight.
//    expireAfterAccess=[duration]: sets Caffeine.expireAfterAccess(java.time.Duration).
//    expireAfterWrite=[duration]: sets Caffeine.expireAfterWrite(java.time.Duration).
//    refreshAfterWrite=[duration]: sets Caffeine.refreshAfterWrite(java.time.Duration).
//    weakKeys: sets Caffeine.weakKeys().
//    weakValues: sets Caffeine.weakValues().
//    softValues: sets Caffeine.softValues().
//    recordStats: sets Caffeine.recordStats().
//    initialCapacity=64,maximumSize=1024,expireAfterAccess=5h
    @Override
    public void afterPropertiesSet() throws Exception {
        if (MapUtils.isEmpty(caffeineSpecs)) return;
        for (Map.Entry<String, String> entry : caffeineSpecs.entrySet()) {
            Caffeine<Object, Object> caffeine = Caffeine.from(entry.getValue())
                    .removalListener((key, value, cause) ->
                            logger.debug(String.format("Key %s was removed (%s)%n", key, cause))
                    );
            CaffeineCache cache = new CaffeineCache(entry.getKey(), caffeine.build(), false);
            if (logger.isTraceEnabled())
                logger.trace(String.format("Finisded init cache=%s by %s", entry.getKey(), entry.getValue()));
            caffeineCache.put(entry.getKey(), cache);
        }
    }

    public void setCaffeineSpecs(Map<String, String> caffeineSpecs) {
        this.caffeineSpecs = caffeineSpecs;
    }
}
