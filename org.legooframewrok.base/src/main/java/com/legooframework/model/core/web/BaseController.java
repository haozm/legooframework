package com.legooframework.model.core.web;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.MessageGateWay;
import com.legooframework.model.core.osgi.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public abstract class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected LoginContext getLoginContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Assert.isInstanceOf(LoginContext.class, principal);
        LoginContextHolder.setCtx((LoginContext) principal);
        return LoginContextHolder.get();
    }

    protected ApplicationContext getAppCtx(HttpServletRequest request) {
        Preconditions.checkNotNull(request);
        return RequestContextUtils.findWebApplicationContext(request);
    }

    protected void loggerRequest(Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] requestBody = %s", request.getRequestURI(), requestBody));
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonMessage defaultErrorHandler(HttpServletRequest req, Exception e) {
        logger.error(e.getMessage(), e);
        return JsonMessageBuilder.ERROR(e).toMessage();
    }

    protected <T> T getBean(String beanName, Class<T> clazz, HttpServletRequest request) {
        return getAppCtx(request).getBean(beanName, clazz);
    }

    protected MessageGateWay getEventBus(HttpServletRequest request) {
        return getAppCtx(request).getBean("messageGateWay", MessageGateWay.class);
    }

    protected Bundle getBundle(String beanName, HttpServletRequest request) {
        return getAppCtx(request).getBean(beanName, Bundle.class);
    }

    protected <T> T getBean(Class<T> clazz, HttpServletRequest request) {
        return getAppCtx(request).getBean(clazz);
    }

}
