package com.csosm.module.base.cache;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class GuavaCacheManager {
    private static final Logger logger = LoggerFactory.getLogger(GuavaCacheManager.class);

    private final Map<String, GuavaCache> cacheMap;

    private final String defaultName;

    GuavaCacheManager(Map<String, GuavaCache> cacheMap, String defaultName) {
        this.cacheMap = ImmutableMap.copyOf(cacheMap);
        this.defaultName = defaultName;
    }

    public void clearDefCache() {
        if (cacheMap.containsKey("adapterCache")) {
            cacheMap.get("adapterCache").invalidateAll();
            if (logger.isDebugEnabled())
                logger.debug(String.format("invalidateAll Cache which Id is %s .", "adapterCache"));
        }
    }

    public void clearByName(String cacheName) {
        if (Strings.isNullOrEmpty(cacheName)) return;
        if (cacheMap.containsKey(cacheName)) {
            cacheMap.get(cacheName).invalidateAll();
            if (logger.isDebugEnabled())
                logger.debug(String.format("invalidateAll Cache which Id is %s .", cacheName));
        }
    }

    public void clearAll() {
        for (Map.Entry<String, GuavaCache> cache : cacheMap.entrySet()) {
            cache.getValue().invalidateAll();
            if (logger.isDebugEnabled())
                logger.debug(
                        String.format("invalidateAll Cache which Id is %s.", cache.getKey()));
        }
    }

    public Optional<GuavaCache> getCache(String name) {
        String _name = Strings.isNullOrEmpty(name) ? defaultName : name;
        return Optional.fromNullable(cacheMap.containsKey(_name) ? cacheMap.get(_name) : null);
    }

    public Collection<GuavaCache> getAllCache() {
        return cacheMap.values();
    }

    public Collection<String> getAllCacheNames() {
        return cacheMap.keySet();
    }

    public boolean exitsCache(String name) {
        return cacheMap.containsKey(name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cacheMap", cacheMap)
                .add("defaultName", defaultName)
                .toString();
    }
}
