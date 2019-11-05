package com.legooframework.model.jwtoken.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("jwtokenBundle", Bundle.class);
    }

}
