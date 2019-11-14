package com.legooframework.model.httpproxy.service;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

public class HttpProxyService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    /**
     * 接受
     *
     * @param payload OXXO
     * @return OOXX
     */
    public Map<String, Object> postProxy(@Header Object haeder, @Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[POST-PROXY] :%s , %s", haeder, payload));
        Map<String, Object> params = Maps.newHashMap();
        params.put("hao", "xiaojie");
        params.put("xiaojie", "hao");
        return params;
    }


}
