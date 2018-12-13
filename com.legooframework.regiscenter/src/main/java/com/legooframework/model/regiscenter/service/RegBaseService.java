package com.legooframework.model.regiscenter.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;

public abstract class RegBaseService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("regiscenterBundle", Bundle.class);
    }

}
