package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Map;
import java.util.Set;

public class TenantsRouteEntity extends BaseEntity<Integer> {

    private final Set<Integer> companyIds;
    private final Map<String, String> postUrls;
    private final String domain;

    TenantsRouteEntity(Set<Integer> companyIds, Map<String, String> postUrls, String domain) {
        super(0);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(companyIds), "公司 ids 不可以为空值...");
        this.companyIds = companyIds;
        this.postUrls = Maps.newHashMap(postUrls);
        this.domain = domain;
    }


    boolean contains(Integer companyId) {
        return companyIds.contains(companyId);
    }

    String getUrl(String name) {
        String value = postUrls.get(name);
        Preconditions.checkState(!Strings.isNullOrEmpty(value), "当前配置不含 %s 对应的URL地址...", name);
        return String.format("%s%s", domain, value);
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyIds", companyIds)
                .add("postUrls", postUrls)
                .add("domain", domain)
                .toString();
    }
}
