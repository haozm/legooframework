package com.legooframework.model.httpproxy.service;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageHeaderAccessor;

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
        MessageHeaderAccessor headerAccessor = MessageHeaderAccessor.getMutableAccessor(message);
        logger.debug(String.format("[headerAccessor.getContentType()] : %s", headerAccessor.getContentType()));
        logger.debug(String.format("[header(host)] : %s", headerAccessor.getHeader("host")));
        logger.debug(String.format("[header(http_requestUrl)] : %s", headerAccessor.getHeader("http_requestUrl")));
        logger.debug(String.format("[header(contentType)] : %s", headerAccessor.getHeader("contentType")));
        logger.debug(String.format("[header(http_requestMethod)] : %s", headerAccessor.getHeader("http_requestMethod")));
        logger.debug(String.format("[header(content-length)] : %s", headerAccessor.getHeader("content-length")));
        logger.debug(String.format("[playload(playload)] : %s", message.getPayload()));
        Map<String, Object> params = Maps.newHashMap();
        params.put("hao", "xiaojie");
        params.put("xiaojie", "hao");
        return params;
    }


}
