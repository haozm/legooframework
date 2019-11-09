package com.legooframework.model.smsresult.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.smsprovider.entity.SMSProviderEntityAction;
import org.springframework.integration.core.MessagingTemplate;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("smsResultBundle", Bundle.class);
    }

    MessagingTemplate getMessagingTemplate() {
        return getBean("smsRltMessagingTemplate", MessagingTemplate.class);
    }

    SMSProviderEntityAction getSmsProviderAction() {
        return getBean(SMSProviderEntityAction.class);
    }

    com.legooframework.model.smsprovider.service.BundleService getSmsService() {
        return getBean(com.legooframework.model.smsprovider.service.BundleService.class);
    }
}
