package com.legooframework.model.httpproxy.service;

import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.httpproxy.entity.HttpGateWayParams;
import com.legooframework.model.httpproxy.entity.HttpRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

import java.util.Optional;

public class HttpProxyService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    /**
     * 监听 HTTP 请求 并且把他们分类处理  路由规则   鉴权的问题 尚未触及
     *
     * @param message OXXO
     * @return OOXX
     */
    public Object httpProxy(Message<?> message) {
        if (message.getPayload() instanceof Exception) {
            return JsonMessageBuilder.ERROR((Exception) message.getPayload()).toMessage();
        }
        HttpRequestDto requestDto = new HttpRequestDto(message);
        Optional<Integer> userId = requestDto.getUserId();
        if (logger.isDebugEnabled())
            logger.debug(String.format("requestDto=%s,userId=%d", requestDto.toString(), userId.orElse(0)));
        if (!requestDto.hasQueryMod()) return "";
        HttpGateWayParams gateWayParams = getHttpGateWayFactory().getTarget(requestDto);
        if (requestDto.isPost()) {
            return getHttpProxyAction().postJsonTarget(gateWayParams, requestDto.getBody().orElse(null));
        }
        if (requestDto.isGet()) {
            return getHttpProxyAction().getJsonTarget(gateWayParams, requestDto.getBody().orElse(null));
        }
        return "";
    }

}
