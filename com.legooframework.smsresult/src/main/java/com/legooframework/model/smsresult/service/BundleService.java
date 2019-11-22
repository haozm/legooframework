package com.legooframework.model.smsresult.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.smsprovider.entity.SMSProviderEntityAction;
import com.legooframework.model.smsprovider.entity.SMSSubAccountEntity;
import com.legooframework.model.smsprovider.service.SmsService;
import org.springframework.integration.core.MessagingTemplate;

import java.util.List;
import java.util.Optional;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("smsResultBundle", Bundle.class);
    }

    MessagingTemplate getMessagingTemplate() {
        return getBean("smsResultMsgTemplate", MessagingTemplate.class);
    }

    private SmsService smsService;

    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }

    SmsService getSmsService() {
        Preconditions.checkNotNull(smsService);
        return smsService;
    }
}
