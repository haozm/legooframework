package com.legooframework.model.crmadapter.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.jwtoken.entity.JWToken;
import com.legooframework.model.jwtoken.entity.JWTokenAction;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CheckTokenExitsFilter extends GenericFilterBean {

    static final String FILTER_APPLIED = "__spring_security_token_exits";
    final static String KEY_HEADER = "Authorization";
    // private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public CheckTokenExitsFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        Preconditions.checkNotNull(this.authenticationEntryPoint, "authenticationEntryPoint cannot be null...");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (request.getAttribute(FILTER_APPLIED) != null) {
            // ensure that filter is only applied once per request
            filterChain.doFilter(request, response);
            return;
        }

        final String token = request.getHeader(KEY_HEADER);
        try {
            if (Strings.isNullOrEmpty(token)) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("Not Exits Token From http-header with key: %s", KEY_HEADER));
                authenticationEntryPoint.commence(request, response, new TokenNotExitsException("Token not exits"));
                return;
            }
            JWToken jwToken;
            try {
                jwToken = jwTokenAction.checkToken(token);
                Preconditions.checkNotNull(jwToken, String.format("Token %s is logout Or timeout...", token));
            } catch (Exception e) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("Token %s is logout Or timeout...", token));
                authenticationEntryPoint.commence(request, response, new TokenNotExitsException("Token not exits"));
                return;
            }
            request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
            request.setAttribute("__spring_security_token", jwToken);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            request.removeAttribute(FILTER_APPLIED);
            request.removeAttribute("__spring_security_token");
        }

    }

    private JWTokenAction jwTokenAction;
    private AuthenticationEntryPoint authenticationEntryPoint;

    public void setJwTokenAction(JWTokenAction jwTokenAction) {
        this.jwTokenAction = jwTokenAction;
    }

}
