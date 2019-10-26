package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Map;

public class TenantsDomainEntity extends BaseEntity<Integer> {

    private final Map<String, Boolean> refCfgs = Maps.newHashMap();
    private final String domain;
    private final boolean defaulted;
    private final Collection<Integer> companyIds;

    public TenantsDomainEntity(String domain, boolean defaulted, Collection<Integer> companyIds) {
        super(0);
        this.domain = domain;
        this.companyIds = companyIds;
        this.defaulted = defaulted;
    }

    boolean isDefaulted() {
        return defaulted;
    }

    public void addRefConfig(String refId, boolean fragment) {
        this.refCfgs.put(refId, fragment);
    }

    boolean contains(Integer companyId) {
        return CollectionUtils.isNotEmpty(companyIds) && companyIds.contains(companyId);
    }

    String getDomain() {
        return domain;
    }

    boolean hasFragment(String refId) {
        if (refCfgs.containsKey(refId)) return refCfgs.get(refId);
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("domain", domain)
                .add("companyIds", companyIds)
                .add("refCfgs", refCfgs)
                .toString();
    }
}
