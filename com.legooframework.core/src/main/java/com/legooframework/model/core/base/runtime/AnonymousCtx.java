package com.legooframework.model.core.base.runtime;

public class AnonymousCtx extends LoginUser {

    public AnonymousCtx(Long tenantId) {
        super(null, null, tenantId);
    }

    public AnonymousCtx() {
        super(null, null, null);
    }

    public AnonymousCtx(Long loginId, String accountNo, Long tenantId) {
        super(loginId, accountNo, tenantId);
    }

    public AnonymousCtx(Long loginId, String accountNo, Long tenantId, Long storeId) {
        super(loginId, accountNo, tenantId);
    }

    @Override
    public final boolean isAnonymous() {
        return true;
    }

}
