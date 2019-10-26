package com.legooframework.model.regiscenter.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;

import java.util.Optional;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("regiscenterBundle", Bundle.class);
    }

    public CrmOrganizationEntity loadCompanyById(Integer companyId) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyByIdWithRest(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s对应的公司不存在...", companyId);
        return company.get();
    }

    CrmStoreEntity loadStoreById(CrmOrganizationEntity company, Integer storeId) {
        Optional<CrmStoreEntity> store = getBean(CrmStoreEntityAction.class).findById(company, storeId);
        Preconditions.checkState(store.isPresent(), "ID=%s 对应的门店不存在...", storeId);
        return store.get();
    }

    CrmStoreEntity loadStoreById(Integer companyId, Integer storeId) {
        CrmOrganizationEntity company = loadCompanyById(companyId);
        Optional<CrmStoreEntity> store = getBean(CrmStoreEntityAction.class).findByIdWitRest(company, storeId);
        Preconditions.checkState(store.isPresent(), "ID=%s 对应的门店不存在...", storeId);
        return store.get();
    }
}
