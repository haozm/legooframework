package com.legooframework.model.base.runtime;

import com.google.common.base.Preconditions;

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
        LoginContext ctx = threadLocal.get();
        return Optional.ofNullable(ctx);
    }

    public static LoginContext getAnonymousCtx() {
        return new AnonymousCtx();
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
        LoginContext ctx = threadLocal.get();
        Preconditions.checkNotNull(ctx, "当前上下文中不含LoginCtx 信息.");
        return threadLocal.get();
    }

}
