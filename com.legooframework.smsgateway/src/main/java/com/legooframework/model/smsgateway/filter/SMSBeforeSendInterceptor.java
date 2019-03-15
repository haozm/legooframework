package com.legooframework.model.smsgateway.filter;

import com.legooframework.model.smsgateway.entity.SMSTransportLogEntity;

import java.util.Collection;
import java.util.List;

public class SMSBeforeSendInterceptor extends SmsSendInterceptor {

    public SMSBeforeSendInterceptor(List<SmsSendInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public boolean filter(Collection<SMSTransportLogEntity> smsTransportLogs) {
        interceptors.forEach(interceptor -> interceptor.filter(smsTransportLogs));
        return true;
    }

    private List<SmsSendInterceptor> interceptors;

}
