package com.csosm.module.sso;

import com.csosm.commons.adapter.LoginUserContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.header.HeaderWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenHeaderWrite implements HeaderWriter {

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        UsernamePasswordAuthenticationToken upat = null;
        UserDetails userDetails = null;
        if (securityContext != null) {
            Object obj = securityContext.getAuthentication();
            if (obj instanceof UsernamePasswordAuthenticationToken) {
                upat = (UsernamePasswordAuthenticationToken) obj;
            }
            if (upat != null) {
                Object user = upat.getPrincipal();
                if (user instanceof UserDetails) {
                    userDetails = (UserDetails) user;
                }
            }
            if (userDetails != null) {
                LoginUserContext loginUserContext = (LoginUserContext) userDetails;
                response.addHeader("Authorization", loginUserContext.getToken());
            }
        }
    }
}
