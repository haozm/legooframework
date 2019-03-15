package com.legooframework.model.crmadapter.entity;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class TenantsRouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(TenantsRouteBuilder.class);

    private Set<Integer> companyIds;
    private Map<String, String> postUrls;
    private String domain;

    public TenantsRouteBuilder(Set<Integer> companyIds, String domain) {
        this.companyIds = companyIds;
        this.domain = domain;
        this.postUrls = Maps.newHashMap();
    }

    public void setPostUrls(String name, String value) {
        this.postUrls.put(name, value);
    }

    public TenantsRouteEntity building() {
        TenantsRouteEntity route = new TenantsRouteEntity(companyIds, postUrls, domain);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Builder : %s", route));
        return route;
    }
}
