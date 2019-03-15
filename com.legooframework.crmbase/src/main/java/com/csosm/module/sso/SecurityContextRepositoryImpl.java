package com.csosm.module.sso;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.BaseModelServer;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legooframework.model.jwtoken.entity.JWToken;
import com.legooframework.model.jwtoken.entity.JWTokenAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

public class SecurityContextRepositoryImpl extends AbstractBaseServer implements SecurityContextRepository {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContextRepositoryImpl.class);

    private final static String KEY_HEADER = "Authorization";
    private final static String KEY_TOKEN_START = "Bearer ";
    private final Cache<String, SecurityContext> securityContextCache;

    public SecurityContextRepositoryImpl() {
        this.securityContextCache = CacheBuilder.newBuilder().initialCapacity(64)
                .maximumSize(2048).expireAfterAccess(2, TimeUnit.HOURS).build();
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        final String token = request.getHeader(KEY_HEADER);
        if (Strings.isNullOrEmpty(token)) {
            if (logger.isWarnEnabled())
                logger.warn(String.format("No SecurityContext tonken: %s A new EmptyContext will be created.",
                        request.getHeader(KEY_HEADER)));
            return SecurityContextHolder.createEmptyContext();
        }
        JWToken jwToken = tokenClient.checkToken(token);
        if (jwToken == null) {
            if (logger.isWarnEnabled())
                logger.warn(String.format("Token is logout Or timeout: %s A new EmptyContext will be created.",
                        request.getHeader(KEY_HEADER)));
            return SecurityContextHolder.createEmptyContext();
        }

        SecurityContext securityContext = this.securityContextCache.getIfPresent(token);
        if (securityContext != null && securityContext.getAuthentication() != null) {
            return securityContext;
        }

        String[] login_info = StringUtils.split(jwToken.getLoginName(), '@');
        LoginUserContext user = baseModelServer.loadByUserName(Integer.valueOf(login_info[0]), login_info[1]);
        user.setToken(token);
        user.setLoginName(jwToken.getLoginName());
        securityContext = createSecurityContext(user);
        securityContextCache.put(token, securityContext);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadContext(%s) return %s", token, securityContext));
        return securityContext;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        if (context.getAuthentication() == null) return;
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) context.getAuthentication();
        LoginUserContext user = (LoginUserContext) authenticationToken.getPrincipal();
        String token = user.getToken();
        if (securityContextCache.getIfPresent(token) == null) {
            securityContextCache.put(token, context);
            if (logger.isDebugEnabled())
                logger.debug(String.format("saveContext(%s,%s)", token, context));
        }
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        final String token = request.getHeader(KEY_HEADER);
        if (Strings.isNullOrEmpty(token)) return false;
        return securityContextCache.getIfPresent(token) != null;
    }

    private SecurityContext createSecurityContext(LoginUserContext user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user,
                user.getPassword());
        return new SecurityContextImpl(authenticationToken);
    }

    public void logout(HttpServletRequest request, Authentication authentication) {
        String token = null;
        if (authentication != null) {
            LoginUserContext user = (LoginUserContext) authentication.getPrincipal();
            token = user.getToken();
        } else if (!Strings.isNullOrEmpty(request.getHeader(KEY_HEADER))) {
            token = request.getHeader(KEY_HEADER);
        }
        if (!Strings.isNullOrEmpty(token)) {
            boolean res = tokenClient.logout(token);
            if (securityContextCache.getIfPresent(token) != null)
                securityContextCache.invalidate(token);
            if (logger.isDebugEnabled())
                logger.debug(String.format("logout(%s) is ok...", token));
        } else {
            logger.warn("当前请求中不含 Token 信息，忽略退出操作....");
        }
    }

    private BaseModelServer baseModelServer;
    private JWTokenAction tokenClient;

    public void setBaseModelServer(BaseModelServer baseModelServer) {
        this.baseModelServer = baseModelServer;
    }

    public void setTokenClient(JWTokenAction tokenClient) {
        this.tokenClient = tokenClient;
    }
}
