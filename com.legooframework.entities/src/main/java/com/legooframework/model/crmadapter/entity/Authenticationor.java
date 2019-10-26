package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Optional;

public class Authenticationor {

    private final LoginContext user;
    private final CrmStoreEntity store;
    private final CrmOrganizationEntity company;
    private final Collection<Integer> storeIds;

    public Authenticationor(LoginContext user, CrmOrganizationEntity company, CrmStoreEntity store) {
        this.store = store;
        this.company = company;
        this.storeIds = null;
        this.user = user;
    }

    public Authenticationor(LoginContext user, CrmOrganizationEntity company, Collection<Integer> storeIds) {
        this.store = null;
        this.company = company;
        this.storeIds = storeIds;
        this.user = user;
    }

    public boolean hasStore() {
        return null != store;
    }

    public boolean hasStores() {
        return CollectionUtils.isNotEmpty(storeIds);
    }

    public boolean isEmpty() {
        return this.store == null && CollectionUtils.isEmpty(storeIds);
    }

    public boolean isNotEmpty() {
        return this.store != null || CollectionUtils.isNotEmpty(storeIds);
    }

    public LoginContext getUser() {
        return user;
    }

    public CrmStoreEntity getStore() {
        Preconditions.checkNotNull(store, "获取门店信息异常，该门店不存在....");
        return store;
    }

    public Collection<Integer> getStoreIds() {
        Preconditions.checkState(CollectionUtils.isNotEmpty(storeIds));
        return storeIds;
    }

    public Optional<Collection<Integer>> getOptStoreIds() {
        return Optional.ofNullable(CollectionUtils.isEmpty(storeIds) ? null : storeIds);
    }

    public CrmOrganizationEntity getCompany() {
        return company;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("company", company)
                .add("store", store)
                .add("storeIds", storeIds)
                .toString();
    }
}
