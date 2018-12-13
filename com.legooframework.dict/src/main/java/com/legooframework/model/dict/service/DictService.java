package com.legooframework.model.dict.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;

public abstract class DictService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("dictBundle", Bundle.class);
    }

}
