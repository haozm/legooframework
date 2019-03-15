package com.legooframework.model.smsgateway.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.smsgateway.entity.SMSTransportLogEntity;
import com.legooframework.model.smsgateway.entity.SMSTransportLogEntityAction;

import java.util.List;
import java.util.Optional;

public class SmsSendingService extends SMSService {

    public List<SMSTransportLogEntity> loadSms4Sending() {
//        LoginContextHolder.setAnonymousCtx();
//        Optional<List<SMSTransportLogEntity>> smsTransportLogs = getBean(SMSTransportLogEntityAction.class).loadSms4Sending();
        return null;
    }

}
