package com.legooframework.model.httpproxy.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.httpproxy.entity.HttpGateWayFactory;
import com.legooframework.model.httpproxy.entity.HttpProxyEntityAction;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("httpPorxyBundle", Bundle.class);
    }

    HttpGateWayFactory getHttpGateWayFactory() {
        return getBean("httpGateWayFactory", HttpGateWayFactory.class);
    }

    HttpProxyEntityAction getHttpProxyAction() {
        return getBean("httpProxyEntityAction", HttpProxyEntityAction.class);
    }
}
