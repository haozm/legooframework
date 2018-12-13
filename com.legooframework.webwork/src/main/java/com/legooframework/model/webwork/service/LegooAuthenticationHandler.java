package com.legooframework.model.webwork.service;

import com.google.common.collect.Maps;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.webwork.entity.LoginTokenEntity;
import com.legooframework.model.webwork.entity.WebUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class LegooAuthenticationHandler extends WebBaseService implements AuthenticationSuccessHandler,
        AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(LegooAuthenticationEntryPoint.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        Object principal = authentication.getPrincipal();
        Assert.isInstanceOf(WebUserDetails.class, principal, String.format("非法的授权用户数据信息:%s", principal));
        Map<String, Object> tokens = Maps.newHashMap();
        LoginTokenEntity tokenEntity = ((WebUserDetails) principal).getLoginToken();
        tokens.put("fullToken", tokenEntity.getFullToken());
        tokens.put("shortToken", tokenEntity.getId());
        JsonMessage message = JsonMessageBuilder.OK().withPayload(tokens).toMessage();
        response.getWriter().write(WebUtils.toJson(message));
        response.flushBuffer();
        clearAuthenticationAttributes(request);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        JsonMessage message = saveException(request, exception);
        response.getWriter().write(WebUtils.toJson(message));
        response.flushBuffer();
        clearAuthenticationAttributes(request);
    }

    protected final void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    protected final JsonMessage saveException(HttpServletRequest request, AuthenticationException exception) {
        if (logger.isErrorEnabled())
            logger.error("Web AuthenticationFailure...", exception);
        return JsonMessageBuilder.ERROR(exception).toMessage();
    }
}
