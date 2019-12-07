package com.legooframework.model.httpproxy.mvc;

import com.google.common.util.concurrent.RateLimiter;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RateLimiterHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterHandlerInterceptor.class);

    @SuppressWarnings("UnstableApiUsage")
    private static final RateLimiter rateLimiter = RateLimiter.create(1);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //noinspection UnstableApiUsage
        if (rateLimiter.tryAcquire()) {
            if (logger.isDebugEnabled())
                logger.debug("资源允许，限流无效.....放行.....");
            return true;
        }
        JsonMessage jsonMessage = JsonMessageBuilder.ERROR("7777", "请求过于频繁...").toMessage();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write(WebUtils.toJson(jsonMessage));
            response.getWriter().flush();
        } catch (Exception e) {
            logger.error(String.format("针对【%s】限流策略异常....", request.getRequestURI()), e);
        } finally {
            if (response.getWriter() != null) response.getWriter().close();
        }
        return false;
    }


}
