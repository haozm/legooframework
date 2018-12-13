package com.legooframework.model.webwork.service;

import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LegooAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(LegooAuthenticationEntryPoint.class);

    public LegooAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {
        logger.error(e.getMessage(), e);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        JsonMessage message = JsonMessageBuilder.NOTLOGIN("not exits token,need login ....").toMessage();
        response.getWriter().write(WebUtils.toJson(message));
        response.flushBuffer();
    }

}
