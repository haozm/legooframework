package com.legooframework.model.core.base.runtime;

import com.google.common.base.Preconditions;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

// 可保证在同一线程下  登陆用户的信息的传递(不支持跨线程)
public class LoginContextHolder {

    private final static ThreadLocal<LoginContext> threadLocal = new ThreadLocal<LoginContext>();

    public static void setCtx(LoginContext ctx) {
        Preconditions.checkNotNull(ctx, "登陆上下文为空值...无法设置...");
        threadLocal.set(ctx);
    }

    public static void clear() {
        threadLocal.remove();
    }

    public static Optional<LoginContext> getIfExits() {
        LoginContext ctx = null;
        Object principal = null;
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        if (null == principal) principal = threadLocal.get();
        if (principal instanceof LoginContext) ctx = (LoginContext) principal;
        return Optional.ofNullable(ctx);
    }

    public static LoginUser getAnonymousCtx() {
        return new LoginUser(-1L, "SecureAnonymous", "*********", -1L, null);
    }

    public static LoginContext getAnonymousCtx(Long tenantId) {
        return new AnonymousCtx(-1L, null, tenantId);
    }

    public static LoginContext getAnonymousCtx(Long loginId, String accountNo, Long tenantId) {
        return new AnonymousCtx(loginId, accountNo, tenantId);
    }

    public static LoginContext setIfNotExitsAnonymousCtx() {
        LoginContext ctx = threadLocal.get();
        return ctx == null ? setAnonymousCtx() : ctx;
    }

    public static LoginContext setAnonymousCtx() {
        LoginContext loginContext = new AnonymousCtx();
        threadLocal.set(loginContext);
        return loginContext;
    }

    public static LoginContext get() {
        LoginContext ctx = null;
        Object principal = null;
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        if (null == principal) principal = threadLocal.get();
        if (principal instanceof LoginContext) ctx = (LoginContext) principal;
        Preconditions.checkNotNull(ctx, "当前上下文中不含LoginCtx 信息.");
        return ctx;
    }

}
