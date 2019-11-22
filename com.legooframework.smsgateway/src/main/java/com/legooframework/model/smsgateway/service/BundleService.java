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
import com.legooframework.model.smsgateway.entity.*;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class BundleService extends BaseService {

    public static String CHANNEL_SMS_BILLING = "channel_sms_billing";

    @Override
    protected Bundle getLocalBundle() {
        return getBean("smsGateWayBundle", Bundle.class);
    }

    StoEntity getStore(Integer storeId) {
        return getBean(StoEntityAction.class).loadById(storeId);
    }

    List<StoEntity> getStores(Collection<Integer> storeIds) {
        Optional<List<StoEntity>> _tems = getBean(StoEntityAction.class).findByIds(storeIds);
        Preconditions.checkState(_tems.isPresent());
        return _tems.get();
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

    SendMsgStateEntityAction sendMsgStateEntityAction;
    RechargeBalanceEntityAction rechargeBalanceEntityAction;
    DeductionDetailEntityAction deductionDetailEntityAction;
    MsgTransportBatchEntityAction msgTransportBatchEntityAction;
    WechatMessageEntityAction wechatMessageEntityAction;
    RechargeDetailEntityAction rechargeDetailEntityAction;
    SendMsg4ReimburseEntityAction reimburseEntityAction;

    public void setReimburseEntityAction(SendMsg4ReimburseEntityAction reimburseEntityAction) {
        this.reimburseEntityAction = reimburseEntityAction;
    }

    public void setRechargeDetailEntityAction(RechargeDetailEntityAction rechargeDetailEntityAction) {
        this.rechargeDetailEntityAction = rechargeDetailEntityAction;
    }

    public void setWechatMessageEntityAction(WechatMessageEntityAction wechatMessageEntityAction) {
        this.wechatMessageEntityAction = wechatMessageEntityAction;
    }

    public void setMsgTransportBatchEntityAction(MsgTransportBatchEntityAction msgTransportBatchEntityAction) {
        this.msgTransportBatchEntityAction = msgTransportBatchEntityAction;
    }

    public void setDeductionDetailEntityAction(DeductionDetailEntityAction deductionDetailEntityAction) {
        this.deductionDetailEntityAction = deductionDetailEntityAction;
    }

    public void setSendMsgStateEntityAction(SendMsgStateEntityAction sendMsgStateEntityAction) {
        this.sendMsgStateEntityAction = sendMsgStateEntityAction;
    }

    public void setRechargeBalanceEntityAction(RechargeBalanceEntityAction rechargeBalanceEntityAction) {
        this.rechargeBalanceEntityAction = rechargeBalanceEntityAction;
    }
}
