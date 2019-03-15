package com.legooframework.model.smsgateway.filter;

import com.legooframework.model.smsgateway.entity.SMSTransportLogEntity;

import java.util.Collection;

public abstract class SmsSendInterceptor {

    public abstract boolean filter(Collection<SMSTransportLogEntity> smsTransportLogs);

}
