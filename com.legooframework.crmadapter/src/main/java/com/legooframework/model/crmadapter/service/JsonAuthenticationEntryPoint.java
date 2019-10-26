package com.legooframework.model.crmadapter.service;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JsonAuthenticationEntryPoint.class);

    private static Gson gson = new Gson();

    public JsonAuthenticationEntryPoint() {
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, Object> params = Maps.newHashMap();
        params.put("code", "9998");
        params.put("msg", exception.getMessage());
        params.put("data", null);
        response.getWriter().write(gson.toJson(params));
        response.flushBuffer();
        if (logger.isDebugEnabled())
            logger.debug(String.format("Token 鉴权失败，返回:%s", params));
    }
}
