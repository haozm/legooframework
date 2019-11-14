package com.legooframework.model.httpproxy.service;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

public class HttpProxyService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    /**
     * 接受
     *
     * @param message OXXO
     * @return OOXX
     */
    public Map<String, Object> postProxy(Message<?> message) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[POST-PROXY] : %s", message));
        Map<String, Object> params = Maps.newHashMap();
        params.put("hao", "xiaojie");
        params.put("xiaojie", "hao");
        return params;
    }


}
