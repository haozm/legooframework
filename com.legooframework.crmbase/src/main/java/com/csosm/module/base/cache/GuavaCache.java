package com.csosm.module.base.cache;

import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.cache.ForwardingCache;

public class GuavaCache extends ForwardingCache<Object, Object> {

    private final Cache<Object, Object> cache;
    private final String name, desc;

    GuavaCache(String name, Cache<Object, Object> cache, String desc) {
        this.name = name;
        this.desc = desc;
        this.cache = cache;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }

    @Override
    protected Cache<Object, Object> delegate() {
        return this.cache;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("desc", desc).toString();
    }
}
