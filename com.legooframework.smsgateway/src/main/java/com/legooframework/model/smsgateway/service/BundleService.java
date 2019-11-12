package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.smsgateway.entity.ChargeSummaryEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeDetailEntityAction;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsprovider.entity.SMSSubAccountEntity;
import com.legooframework.model.smsprovider.service.SendedSmsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
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

    private String smsGateWayUrl;

    public void setSmsGateWayUrl(String smsGateWayUrl) {
        this.smsGateWayUrl = smsGateWayUrl;
    }

    String getSendSmsApi() {
        return String.format("%s/api/%s", smsGateWayUrl, "smses/batch/sending.json?_format={format}");
    }

    String getSmsBlackApi() {
        return String.format("%s/api/%s", smsGateWayUrl, "blacklist/sms/sync.json");
    }

    String getFinalStateApi() {
        return String.format("%s/api/%s", smsGateWayUrl, "finalstate/fetching/bymsgId.json");
    }


    String sendByProxy(String postUrl, String payload) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("payload", payload);
        return post("http://testold.csosm.com/smsresult/api/smses/batch/sending.json", params);
    }

    private String post(String postUrl, Object payload) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(postUrl)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(payload)
                .retrieve().bodyToMono(String.class);
        String response = mono.block(Duration.ofSeconds(20L));
        stopwatch.stop(); // optional
        if (logger.isDebugEnabled())
            logger.debug(String.format("post(postUrl=%s,payload=%s) return %s [%s]", postUrl, payload, response, stopwatch));
        return response;
    }
}
