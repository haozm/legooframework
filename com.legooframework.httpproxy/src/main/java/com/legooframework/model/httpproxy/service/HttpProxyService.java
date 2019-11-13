package com.legooframework.model.httpproxy.service;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public Map<String, Object> postProxy(@Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("Http-reqest : %s", payload));
        Map<String, Object> params = Maps.newHashMap();
        params.put("hao", "xiaojie");
        params.put("xiaojie", "hao");
        return params;
    }


}
