package com.legooframework.model.smsprovider.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.smsprovider.entity.SMSProviderEntityAction;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("smsProviderBundle", Bundle.class);
    }

    SMSProviderEntityAction getSMSProvider() {
        return getBean(SMSProviderEntityAction.class);
    }

}
