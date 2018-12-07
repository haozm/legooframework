package com.legooframework.model.base.runtime;

import com.google.common.collect.Sets;

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
        setStores(Sets.newHashSet(LegooOrgImpl.store(storeId, "Anonymous", null)));
    }

    @Override
    public final boolean isAnonymous() {
        return true;
    }

}
