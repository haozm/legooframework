package com.legooframework.model.webwork.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LegooLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        securityContextRepository.clearCtx(request);
        super.onLogoutSuccess(request, response, authentication);
    }

    private LegooSecurityContextRepository securityContextRepository;

    public void setSecurityContextRepository(LegooSecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

}
