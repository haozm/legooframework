package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.core.ResolvableType;

import java.util.Map;
import java.util.Optional;

public abstract class BaseEntityAction<T extends BaseEntity> {

    private static final Logger logger = LoggerFactory.getLogger(BaseEntityAction.class);
    final static String KEY_HEADER = "Authorization";
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

//    TenantsRouteFactory getTenantsRouteFactory() {
//        return tenantsRouteFactory;
//    }

    Optional<JsonElement> post(Integer companyId, String action, Map<String, Object> params, Object... pathVariables) {
//        String postUrl = tenantsRouteFactory.getUrl(companyId, action);
//        return tenantsRouteFactory.post(postUrl, params, pathVariables);
        return null;
    }

    Optional<JsonElement> postWithRest(Integer companyId, String action, Map<String, Object> params, Object... pathVariables) {
//        String postUrl = tenantsRouteFactory.getUrl(companyId, action);
//        return tenantsRouteFactory.postWithRest(postUrl, params, pathVariables);
        return null;
    }

    Optional<JsonElement> postWithToken(Integer companyId, String action, String token, Map<String, Object> params,
                                        Object... pathVariables) {
//        String url = tenantsRouteFactory.getUrl(companyId, action);
//        return tenantsRouteFactory.post(url, token, params, pathVariables);
        return null;
    }

    private CaffeineCacheManager cacheManager;

   // private TenantsRouteFactory tenantsRouteFactory;

//    public void setTenantsRouteFactory(TenantsRouteFactory tenantsRouteFactory) {
//        this.tenantsRouteFactory = tenantsRouteFactory;
//    }

    public void setCacheManager(CaffeineCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

}
