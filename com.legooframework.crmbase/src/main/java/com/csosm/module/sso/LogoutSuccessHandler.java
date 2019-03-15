package com.csosm.module.sso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        securityContextRepository.logout(request, authentication);
        super.onLogoutSuccess(request, response, authentication);
        if (logger.isDebugEnabled())
            logger.debug(String.format("登出:onLogoutSuccess(%s)", authentication));
    }

    private SecurityContextRepositoryImpl securityContextRepository;

    public void setSecurityContextRepository(SecurityContextRepositoryImpl securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }
}
