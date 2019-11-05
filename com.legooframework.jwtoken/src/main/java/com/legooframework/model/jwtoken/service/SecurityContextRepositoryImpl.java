package com.legooframework.model.jwtoken.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.jwtoken.entity.JWToken;
import com.legooframework.model.jwtoken.entity.JWTokenAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

public class SecurityContextRepositoryImpl extends BundleService implements SecurityContextRepository {

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
        final String tokenId = request.getHeader(KEY_HEADER);
        SecurityContext securityContext = this.securityContextCache.getIfPresent(tokenId);
        if (securityContext != null && securityContext.getAuthentication() != null) {
            return securityContext;
        }
        final JWToken jwToken = (JWToken) request.getAttribute("__spring_security_token");
        Preconditions.checkNotNull(jwToken, "非法的 Token取值....,token值为空...");
        LoginUser user = null;
        if (jwToken.isAnonymous()) {
            user = LoginContextHolder.getAnonymousCtx();
            user.setToken(tokenId);
            user.setLoginName(jwToken.getLoginName());
        } else {
            String loginName = jwToken.getLoginName();
            String[] login_info = StringUtils.split(loginName, '@');
            // user = getBean(CrmReadService.class).loadByLoginName(Integer.valueOf(login_info[0]), login_info[1]);
            user.setToken(tokenId);
            user.setLoginName(loginName);
        }
        securityContext = createSecurityContext(user);
        securityContextCache.put(tokenId, securityContext);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadContext(%s) return %s", tokenId, securityContext));
        return securityContext;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        if (context.getAuthentication() == null) return;
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) context.getAuthentication();
        LoginUser user = (LoginUser) authenticationToken.getPrincipal();
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

    private SecurityContext createSecurityContext(LoginUser user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user,
                user.getPassword());
        return new SecurityContextImpl(authenticationToken);
    }

    void logout(HttpServletRequest request, Authentication authentication) {
        String token = null;
        if (authentication != null) {
            LoginUser user = (LoginUser) authentication.getPrincipal();
            token = user.getToken();
        } else if (!Strings.isNullOrEmpty(request.getHeader(KEY_HEADER))) {
            token = request.getHeader(KEY_HEADER);
        }
        if (!Strings.isNullOrEmpty(token)) {
            boolean res = getJWTokenAction().logout(token);
            if (securityContextCache.getIfPresent(token) != null)
                securityContextCache.invalidate(token);
            if (logger.isDebugEnabled())
                logger.debug(String.format("logout(%s) is ok...", token));
        } else {
            logger.warn("当前请求中不含 Token 信息，忽略退出操作....");
        }
    }

    JWTokenAction getJWTokenAction() {
        return getBean(JWTokenAction.class);
    }
}
