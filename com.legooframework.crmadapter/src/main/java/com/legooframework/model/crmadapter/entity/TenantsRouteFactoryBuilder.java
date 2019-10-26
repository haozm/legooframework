package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TenantsRouteFactoryBuilder {

    private final List<TenantsRouteFactory.UrlItem> urlItems = Lists.newArrayList();
    private final Map<String, TenantsDomainEntity> domainEntityMap = Maps.newHashMap();


    TenantsRouteFactoryBuilder() {
    }

    public void addUrlItem(TenantsRouteFactory.UrlItem item) {
        if (this.urlItems.contains(item)) return;
        this.urlItems.add(item);
    }

    public void addDomainEntity(TenantsDomainEntity domainEntity) {
        Preconditions.checkState(!domainEntityMap.containsKey(domainEntity.getDomain()), "已经存在对应的域名定义 %s",
                domainEntity.getDomain());
        this.domainEntityMap.put(domainEntity.getDomain(), domainEntity);
    }

    List<TenantsRouteFactory.UrlItem> getUrlItems() {
        return urlItems;
    }

    Collection<TenantsDomainEntity> getDomains() {
        return domainEntityMap.values();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("urlItems", urlItems)
                .add("domainEntityMap", domainEntityMap)
                .toString();
    }
}
