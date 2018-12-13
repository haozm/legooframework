package com.legooframework.model.webwork.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;

public abstract class WebBaseService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("webworkBundle", Bundle.class);
    }
}
