package com.legooframework.model.smsgateway.filter;

import com.google.common.base.Charsets;
import com.legooframework.model.smsgateway.entity.SendMsg4SendEntity;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class EncodeUrlCheckInterceptor extends SmsSendInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(EncodeUrlCheckInterceptor.class);

    public EncodeUrlCheckInterceptor() {
    }

    @Override
    public boolean filter(Collection<SendMsg4SendEntity> batchSendLogs) {
//        batchSendLogs.stream().filter(x -> !x.isError()).forEach(x -> {
//            try {
//                urlCodec.encode(x.getSms().getContent());
//            } catch (Exception e) {
//                logger.error(String.format("EncodeUrlCheckInterceptor hander %s has error",
//                        x.getSms().getContent()), e);
//                x.error4Encode();
//            }
//        });
        return false;
    }

    private final URLCodec urlCodec = new URLCodec(Charsets.UTF_8.toString());
}
