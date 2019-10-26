package com.legooframework.model.smsgateway.filter;

import com.legooframework.model.smsgateway.entity.SendMsg4SendEntity;

import java.util.Collection;

public abstract class SmsSendInterceptor {

    public abstract boolean filter(Collection<SendMsg4SendEntity> smsTransportLogs);

}
