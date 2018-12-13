package com.legooframework.model.webwork.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LegooOrg;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.utils.ExceptionUtil;
import com.legooframework.model.core.utils.WkSessionUtil;
import com.legooframework.model.organization.dto.EmployeeAgg;
import com.legooframework.model.organization.entity.StoreEntity;
import com.legooframework.model.organization.event.OrgEventFactory;
import com.legooframework.model.security.dto.AccountAgg;
import com.legooframework.model.security.event.SecEventFactory;
import com.legooframework.model.webwork.entity.LoginTokenAction;
import com.legooframework.model.webwork.entity.LoginTokenEntity;
import com.legooframework.model.webwork.entity.WebUserDetails;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LegooSecurityContextRepository extends WebBaseService implements SecurityContextRepository,
        UserDetailsService, HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LegooSecurityContextRepository.class);

    private final Cache<String, SecurityContext> securityContextCache;

    public static final String CSOSM_TOKEN_KEY = "cssessionid";

    private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    public LegooSecurityContextRepository() {
        this.securityContextCache = Caffeine.newBuilder().initialCapacity(64)
                .maximumSize(1024).expireAfterAccess(1, TimeUnit.HOURS).recordStats().build();
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse,
                                   WebSocketHandler webSocketHandler, Map<String, Object> attributes) throws Exception {
        Optional<String> token = WkSessionUtil.getToken(serverHttpRequest);
        if (token.isPresent()) {
            Optional<WebUserDetails> user = loadByToken(token.get());
            if (user.isPresent()) {
                WkSessionUtil.setOnlineStatus(attributes);
                WkSessionUtil.setUser(attributes, user.get());
            } else {
                WkSessionUtil.setOfflineStatus(attributes);
            }
            return true;
        } else {
            logger.warn(String.format("当前请求%s未提供合法的token，拒绝连接....", serverHttpRequest.getURI()));
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse serverHttpResponse,
                               WebSocketHandler webSocketHandler, Exception e) {
        logger.error(String.format("afterHandshake(request=%s,wsHandler,exception=%s)", request.getURI(),
                e.getMessage()), e);
    }

    public void clearCtx(HttpServletRequest request) {
        String csosm_token = request.getParameter(CSOSM_TOKEN_KEY);
        if (Strings.isNullOrEmpty(csosm_token)) {
            if (logger.isDebugEnabled())
                logger.debug("无法获取当前 HttpServletRequest 对应的 csosm_token,忽略本次退出请求....");
            return;
        }
        this.securityContextCache.invalidate(csosm_token);
        Optional<LoginTokenEntity> loginToken = getBean(LoginTokenAction.class).findOnlineByToken(csosm_token);
        loginToken.ifPresent(x -> {
            this.securityContextCache.invalidate(x.getFullToken());
            getBean(LoginTokenAction.class).logout(x.getId());
        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("csosm_token = %s 对应的账号成功退出...", csosm_token));
    }

    private Optional<WebUserDetails> loadByToken(String token) {
        if (Strings.isNullOrEmpty(token)) return Optional.empty();
        SecurityContext sec = this.securityContextCache.getIfPresent(token);
        if (sec != null) return Optional.of((WebUserDetails) sec.getAuthentication().getPrincipal());
        Optional<LoginTokenEntity> loginToken = getBean(LoginTokenAction.class).findOnlineByToken(token);
        if (!loginToken.isPresent()) return Optional.empty();
        WebUserDetails user = loadUserByUsernameAndDevice(loginToken.get().getAccountNo(),
                loginToken.get().getDeviceId(), loginToken.get());
        buildSecurityContextByUserDetail(loginToken.get(), user);
        return Optional.of(user);
    }

    @Override
    public UserDetails loadUserByUsername(String accountWithDeviceNo) throws UsernameNotFoundException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountWithDeviceNo),
                "登陆认证账户信息为空...");
        String[] loginInfo = StringUtils.split(accountWithDeviceNo, '@');
        Preconditions.checkArgument(loginInfo.length == 2,
                "非法的登陆账户信息%s...,[account@deviceno]", accountWithDeviceNo);
        return loadUserByUsernameAndDevice(loginInfo[0], loginInfo[1], null);
    }

    private WebUserDetails loadUserByUsernameAndDevice(String username, String deviceno, LoginTokenEntity exitsToken)
            throws UsernameNotFoundException {
        try {
            LegooEvent event = SecEventFactory.loadAccountAggByNoEvent(getLocalBundle(), username);
            LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx());
            Optional<AccountAgg> accountAgg = getEventBus().sendAndReceive(event, AccountAgg.class);
            if (logger.isTraceEnabled())
                logger.trace(String.format("accountAgg by %s is %s", username, accountAgg.orElse(null)));
            if (!accountAgg.isPresent())
                throw new UsernameNotFoundException(String.format("无法获取%s对应的账户信息", username));
            event = OrgEventFactory.loadEmployeeAggEvent(getLocalBundle(), accountAgg.get().getAccount());
            Optional<EmployeeAgg> employeeAgg = getEventBus().sendAndReceive(event, EmployeeAgg.class);
            if (!employeeAgg.isPresent())
                throw new UsernameNotFoundException(String.format("账户%s对应的职员信息无法获取.", username));
            Optional<Collection<StoreEntity>> stores = employeeAgg.get().getStores();
            Collection<LegooOrg> legooOrgs = stores.<Collection<LegooOrg>>map(storeEntities ->
                    storeEntities.stream().map(StoreEntity::toLegooOrg).collect(Collectors.toList())).orElse(null);
            LoginTokenEntity loginToken = exitsToken;
            if (null == loginToken) {
                Optional<LoginTokenEntity> loginTokenOpt = getBean(LoginTokenAction.class).findLastOnlineTokenByAccount(username);
                if (loginTokenOpt.isPresent() && StringUtils.equals(loginTokenOpt.get().getDeviceId(), deviceno)) {
                    loginToken = loginTokenOpt.get();
                }
            }

            List<String> storeDevices = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(legooOrgs)) {
                legooOrgs.forEach(x -> x.getDeviceIds().ifPresent(storeDevices::addAll));
            }
            return new WebUserDetails(accountAgg.get().getAccount(), accountAgg.get().getLegooRoles().orElse(null),
                    employeeAgg.get().getEmployee(), legooOrgs, employeeAgg.get().getCompany(), deviceno, loginToken,
                    storeDevices);
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, UsernameNotFoundException.class);
            String msg = String.format("loadUserByUsername(String %s, String %s, isloing:%s) has error", username
                    , deviceno, exitsToken == null);
            logger.error(msg, e);
            throw ExceptionUtil.handleException(e, msg, logger);
        }
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        Optional<SecurityContext> context = readSecurityContextFromLocal(request);
        if (context.isPresent()) return context.get();
        if (logger.isWarnEnabled())
            logger.warn(String.format("No SecurityContext tonken: %s A new EmptyContext will be created.",
                    request.getHeader(CSOSM_TOKEN_KEY)));
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        SaveContextOnUpdateOrErrorResponseWrapper responseWrapper =
                new SaveToCacheResponseWrapper(requestResponseHolder.getResponse(), emptyContext, securityContextCache);
        requestResponseHolder.setResponse(responseWrapper);
        return emptyContext;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        SaveContextOnUpdateOrErrorResponseWrapper responseWrapper = WebUtils
                .getNativeResponse(response, SaveContextOnUpdateOrErrorResponseWrapper.class);
        if (responseWrapper == null) {
            if (logger.isWarnEnabled())
                logger.warn("Cannot invoke saveContext on response with responseWrapper = null");
            return;
//            throw new IllegalStateException(
//                    "Cannot invoke saveContext on response "
//                            + response
//                            + ". You must use the HttpRequestResponseHolder.response after invoking loadContext");
        }
        if (!responseWrapper.isContextSaved()) {
            ((SaveToCacheResponseWrapper) responseWrapper).saveContext(context);
        }
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        Optional<SecurityContext> context = readSecurityContextFromLocal(request);
        return context.isPresent();
    }

    private SecurityContext buildSecurityContextByUserDetail(LoginTokenEntity loginToken, UserDetails userDetail) {
        String pwd = userDetail.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetail,
                pwd.substring(pwd.indexOf('}') + 1));
        SecurityContext securityContext = new SecurityContextImpl(authenticationToken);
        this.securityContextCache.put(loginToken.getId(), securityContext);
        this.securityContextCache.put(loginToken.getFullToken(), securityContext);
        return securityContext;
    }

    /**
     * @param request the session obtained from the request.
     */
    private Optional<SecurityContext> readSecurityContextFromLocal(HttpServletRequest request) {
        String csosm_token = request.getHeader(CSOSM_TOKEN_KEY);
        if (Strings.isNullOrEmpty(csosm_token)) return Optional.empty();

        Optional<SecurityContext> securityContextOpt = Optional.ofNullable(this.securityContextCache.getIfPresent(csosm_token));
        if (securityContextOpt.isPresent()) return securityContextOpt;

        Optional<LoginTokenEntity> loginToken = getBean(LoginTokenAction.class).findOnlineByToken(csosm_token);
        if (!loginToken.isPresent()) return Optional.empty();
        try {
            UserDetails userDetail = loadUserByUsernameAndDevice(loginToken.get().getAccountNo(),
                    loginToken.get().getDeviceId(), loginToken.get());
            SecurityContext securityContext = buildSecurityContextByUserDetail(loginToken.get(), userDetail);
            return Optional.of(securityContext);
        } catch (Exception e) {
            logger.error(String.format("通过token=%s 还原登陆状态发生异常...", loginToken.get()), e);
            throw new RuntimeException(e);
        }
    }

    final class SaveToCacheResponseWrapper extends SaveContextOnUpdateOrErrorResponseWrapper {

        private final SecurityContext contextBeforeExecution;
        private final Authentication authBeforeExecution;
        private final Cache<String, SecurityContext> cacheProxy;

        SaveToCacheResponseWrapper(HttpServletResponse response, SecurityContext context,
                                   Cache<String, SecurityContext> cacheProxy) {
            super(response, false);
            this.cacheProxy = cacheProxy;
            this.contextBeforeExecution = context;
            this.authBeforeExecution = context.getAuthentication();
        }

        @Override
        public void saveContext(SecurityContext context) {
            final Authentication authentication = context.getAuthentication();
            // See SEC-776
            if (authentication == null || trustResolver.isAnonymous(authentication)) {
                if (logger.isDebugEnabled())
                    logger.debug("SecurityContext is empty or contents are anonymous - context will not be stored in HttpSession.");
                return;
            }

            Object principal = authentication.getPrincipal();
            Assert.isInstanceOf(WebUserDetails.class, principal, String.format("非法的登陆用户类型 %s ....", principal));
            WebUserDetails webUser = (WebUserDetails) principal;
            Optional<LoginTokenEntity> lastOnlineTokenByAccount = getBean(LoginTokenAction.class)
                    .findLastOnlineTokenByAccount(webUser.getAccountNo());
            // 同一台设备 同一个账号 多次登陆且没有退出 情况
            boolean relpace = false;
            if (lastOnlineTokenByAccount.isPresent()) {
                if (StringUtils.equals(lastOnlineTokenByAccount.get().getDeviceId(), webUser.getDeviceNo())) {
                    cacheProxy.put(lastOnlineTokenByAccount.get().getId(), context);
                    cacheProxy.put(lastOnlineTokenByAccount.get().getFullToken(), context);
                    if (logger.isWarnEnabled())
                        logger.warn(String.format("%s 有效，复用本次token....", lastOnlineTokenByAccount.get()));
                    return;
                } else {
                    getBean(LoginTokenAction.class).logout(lastOnlineTokenByAccount.get().getId());
                    cacheProxy.invalidate(lastOnlineTokenByAccount.get().getId());
                    cacheProxy.invalidate(lastOnlineTokenByAccount.get().getFullToken());
                    relpace = true;
                }
            }
            LoginTokenEntity new_login_token = webUser.getLoginToken();
            String token = getBean(LoginTokenAction.class).insert(new_login_token);
            cacheProxy.put(token, context);
            cacheProxy.put(new_login_token.getFullToken(), context);
        }

        private boolean contextChanged(SecurityContext context) {
            return context != contextBeforeExecution
                    || context.getAuthentication() != authBeforeExecution;
        }

    }
}
