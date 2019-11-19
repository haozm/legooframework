package com.legooframework.model.httpproxy.service;

import com.legooframework.model.httpproxy.entity.HttpGateWayParams;
import com.legooframework.model.httpproxy.entity.HttpRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

import java.util.Optional;

public class HttpProxyService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    /**
     * @param message OXXO
     * @return OOXX
     */
    public Object postProxy(Message<?> message) {
        HttpRequestDto requestDto = new HttpRequestDto(message);
        Optional<Integer> userId = requestDto.getUserId();
        if (logger.isDebugEnabled())
            logger.debug(String.format("requestDto=%s,userId=%d", requestDto.toString(), userId.orElse(0)));
        HttpGateWayParams gateWayParams = getHttpGateWayFactory().getTarget(requestDto);
        if (requestDto.isPost()) {
            return getHttpProxyAction().postJsonTarget(gateWayParams, requestDto.getBody().orElse(null));
        }
        return "";
    }

}
