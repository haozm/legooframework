package com.legooframework.model.smsresult.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.smsprovider.service.SmsService;
import org.springframework.integration.core.MessagingTemplate;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("smsResultBundle", Bundle.class);
    }

    MessagingTemplate getMessagingTemplate() {
        return getBean("smsResultMsgTemplate", MessagingTemplate.class);
    }

    SmsService getSmsService() {
        return getBean("smsService", SmsService.class);
    }
}
