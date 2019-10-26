package com.legooframework.model.crmjob.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;

public class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("crmJobBundle", Bundle.class);
    }


}
