package com.legooframework.model.crmadapter.service;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.jwtoken.entity.JWTokenAction;
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

public class AuthenticationService extends CrmAdapterService implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService() {
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, Object> params = Maps.newHashMap();
        params.put("code", "9998");
        params.put("msg", exception.getMessage());
        params.put("data", null);
        response.getWriter().write(WebUtils.toJson(params));
        response.flushBuffer();
        clearAuthenticationAttributes(request);
        if (logger.isDebugEnabled())
            logger.debug(String.format("登陆授权失败，返回:%s", params));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        Object principal = authentication.getPrincipal();
        Assert.isInstanceOf(LoginUser.class, principal, String.format("非法的授权用户数据信息:%s", principal));
        LoginUser user = (LoginUser) principal;

        String token = getJWTokenAction().loginByWeb(user.getUsername(), null);
        user.setToken(token);

        // response.setHeader("Authorization", user.getToken());
        Map<String, Object> params = Maps.newHashMap();
        params.put("code", "0000");
        params.put("msg", "");
        params.put("data", user.getToken());

        response.getWriter().write(WebUtils.toJson(params));
        response.flushBuffer();
        clearAuthenticationAttributes(request);
        if (logger.isDebugEnabled())
            logger.debug(String.format("登陆授权成功，返回:%s", params));
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    JWTokenAction getJWTokenAction() {
        return getBean(JWTokenAction.class);
    }

}
