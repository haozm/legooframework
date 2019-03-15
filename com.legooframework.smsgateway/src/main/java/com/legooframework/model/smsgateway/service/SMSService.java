package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.*;
import com.legooframework.model.smsgateway.entity.ChargeSummaryEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeDetailEntityAction;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class SMSService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("smsGateWayBundle", Bundle.class);
    }

    CrmStoreEntity getStore(Integer companyId, Integer storeId) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        Optional<CrmStoreEntity> stores = getBean(CrmStoreEntityAction.class).findById(company.get(), storeId);
        Preconditions.checkState(stores.isPresent(), "ID=%s 对应的门店不存在...", storeId);
        return stores.get();
    }

    CrmOrganizationEntity getCompany(Integer companyId) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        return company.get();
    }

    Optional<List<CrmStoreEntity>> getStores(Integer companyId, List<Integer> storeIds) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        return getBean(CrmStoreEntityAction.class).findByIds(company.get(), storeIds);
    }

    Optional<List<CrmStoreEntity>> getSubStores(Integer companyId, Integer orgId) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        Optional<CrmOrganizationEntity> org = getBean(CrmOrganizationEntityAction.class)
                .loadOrganization(company.get(), orgId);
        Preconditions.checkState(org.isPresent());
        return getBean(CrmStoreEntityAction.class).loadStoresByOrg(org.get());
    }

    CrmEmployeeEntity getEmployee(Integer companyId, Integer employeeId) {
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        Optional<CrmEmployeeEntity> employee = getBean(CrmEmployeeEntityAction.class).findById(company.get(), employeeId);
        Preconditions.checkState(employee.isPresent(), "ID=%s 对应的职员不存在...", employeeId);
        return employee.get();
    }

    RechargeDetailEntityAction getRechargeAction() {
        return getBean(RechargeDetailEntityAction.class);
    }

    ChargeSummaryEntityAction getSummaryAction() {
        return getBean(ChargeSummaryEntityAction.class);
    }

    TransactionStatus startTx(String txName) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        String tx_name = Strings.isNullOrEmpty(txName) ? UUID.randomUUID().toString() : txName;
        def.setName(tx_name);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return getBean("transactionManager", DataSourceTransactionManager.class).getTransaction(def);
    }

    void commitTx(TransactionStatus status) {
        Preconditions.checkNotNull(status);
        getBean("transactionManager", DataSourceTransactionManager.class).commit(status);
    }

    void rollbackTx(TransactionStatus status) {
        Preconditions.checkNotNull(status);
        getBean("transactionManager", DataSourceTransactionManager.class).rollback(status);
    }


    MessagingTemplate getMessagingTemplate() {
        return getBean("smsMessagingTemplate", MessagingTemplate.class);
    }
}
