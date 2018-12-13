package com.legooframework.model.crmadapter.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;

public abstract class CrmService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("crmAdapterBundle", Bundle.class);
    }
}
