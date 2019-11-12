package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.smsgateway.entity.ChargeSummaryEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeDetailEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.UUID;

public abstract class BundleService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(BundleService.class);

    public static String CHANNEL_SMS_BILLING = "channel_sms_billing";
    static String CHANNEL_SMS_SENDED = "channel_sms_sended";
    static String CHANNEL_SMS_SENDING = "channel_sms_sending";

    @Override
    protected Bundle getLocalBundle() {
        return getBean("smsGateWayBundle", Bundle.class);
    }

    StoEntity getStore(Integer companyId, Integer storeId) {
        return getBean(StoEntityAction.class).loadById(storeId);
    }

    JdbcQuerySupport getJdbcQuerySupport() {
        return getBean("smsJdbcQuerySupport", JdbcQuerySupport.class);
    }

    OrgEntity getCompany(Integer companyId) {
        return getBean(OrgEntityAction.class).loadComById(companyId);
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
