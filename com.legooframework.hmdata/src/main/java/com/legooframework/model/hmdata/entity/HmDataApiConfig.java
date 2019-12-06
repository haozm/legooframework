package com.legooframework.model.hmdata.entity;


import com.google.common.base.MoreObjects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmDataApiConfig {

    @Value(value = "${hmdata.domain}")
    private String domain;
    @Value(value = "${hmdata.cardBindSign}")
    private String cardBindSign;
    @Value(value = "${hmdata.cardBindVerify}")
    private String cardBindVerify;
    @Value(value = "${hmdata.quickPayment}")
    private String quickPayment;
    @Value(value = "${hmdata.queryOrder}")
    private String queryOrder;
    @Value(value = "${hmdata.cardBindRelieve}")
    private String cardBindRelieve;
    @Value(value = "${hmdata.drawApply}")
    private String drawApply;
    @Value(value = "${hmdata.smsSend}")
    private String smsSend;
    @Value(value = "${private.key}")
    private String privateKey;
    @Value(value = "${public.key}")
    private String publicKey;

    public HmDataApiConfig() {
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getCardBindSign() {
        return String.format("%s%s", domain, cardBindSign);
    }

    public String getCardBindVerify() {
        return String.format("%s%s", domain, cardBindVerify);
    }

    public String getQuickPayment() {
        return String.format("%s%s", domain, quickPayment);
    }

    public String getQueryOrder() {
        return String.format("%s%s", domain, queryOrder);
    }

    public String getCardBindRelieve() {
        return String.format("%s%s", domain, cardBindRelieve);
    }

    public String getDrawApply() {
        return String.format("%s%s", domain, drawApply);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("domain", domain)
                .add("cardBindSign", cardBindSign)
                .add("cardBindVerify", cardBindVerify)
                .add("quickPayment", quickPayment)
                .add("queryOrder", queryOrder)
                .add("cardBindRelieve", cardBindRelieve)
                .add("drawApply", drawApply)
                .add("smsSend", smsSend)
                .add("privateKey", "******")
                .add("publicKey", "******")
                .toString();
    }
}
