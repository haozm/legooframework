package com.legooframework.model.smsgateway.filter;

import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import com.legooframework.model.smsgateway.entity.SendMsg4SendEntity;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileNumberCheckInterceptor extends SmsSendInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MobileNumberCheckInterceptor.class);

    public MobileNumberCheckInterceptor(KvDictEntityAction kvDictEntityAction) {
        this.kvDictEntityAction = kvDictEntityAction;
    }

    @Override
    public boolean filter(Collection<SendMsg4SendEntity> smsTransportLogs) {
        final KvDictEntity whitelist = kvDictEntityAction.loadByValue("SMS_PREFIX", "WHITELIST");
        final Pattern pattern = Pattern.compile(whitelist.getDesc());

        smsTransportLogs.stream().filter(x -> !x.isEnbaled()).forEach(SendMsg4SendEntity::error4Init);

        smsTransportLogs.stream().filter(x -> !x.isError()).forEach(x -> {
            String phoneNo = x.getSms().getPhoneNo();
            try {
                if (!validity(phoneNo) || !matcher(phoneNo, pattern)) {
                    x.errorByMobile();
                }
            } catch (Exception e) {
                logger.error(String.format("MobileNumberCheckInterceptor hander %s has error",
                        x.getSms().getPhoneNo()), e);
                x.errorByException(e);
            }
        });
        return false;
    }

    private boolean validity(String phoneNo) {
        return phoneNo.length() == 11 && NumberUtils.isDigits(phoneNo);
    }

    private boolean matcher(String phoneNo, Pattern pattern) {
        Matcher matcher = pattern.matcher(phoneNo);
        return matcher.matches();
    }

    private KvDictEntityAction kvDictEntityAction;

}
