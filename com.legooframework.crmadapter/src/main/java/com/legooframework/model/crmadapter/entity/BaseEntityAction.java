package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

public abstract class BaseEntityAction<T extends BaseEntity> {

    private static final Logger logger = LoggerFactory.getLogger(BaseEntityAction.class);

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected BaseEntityAction(String cacheName) {
        this.cacheName = cacheName;
        this.entityClass = (Class<T>) ResolvableType.forClass(this.getClass())
                .as(BaseEntityAction.class).getGeneric(0).resolve();
        Preconditions.checkNotNull(entityClass, "无法获取 %s 对应的<T extends BaseEntity>泛型参数.");
        this.modelName = entityClass.getSimpleName();
    }

    protected String getModelName() {
        return modelName;
    }

    protected Optional<Cache> getCache() {
        if (Strings.isNullOrEmpty(this.cacheName)) return Optional.empty();
        if (cacheManager == null) return Optional.empty();
        return Optional.ofNullable(cacheManager.getCache(cacheName));
    }

    private final String cacheName;
    private final String modelName;

    TenantsRouteFactory getTenantsRouteFactory() {
        return tenantsRouteFactory;
    }

    Optional<JsonElement> post(String postUrl, Map<String, Object> params, Object... pathVariables) {
        return tenantsRouteFactory.post(postUrl, params, pathVariables);
    }

    private CaffeineCacheManager cacheManager;

    private TenantsRouteFactory tenantsRouteFactory;

    public void setTenantsRouteFactory(TenantsRouteFactory tenantsRouteFactory) {
        this.tenantsRouteFactory = tenantsRouteFactory;
    }

    public void setCacheManager(CaffeineCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

}
