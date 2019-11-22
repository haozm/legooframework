package com.legooframework.model.reactor.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.SendSmsEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.TemplateEntity;
import org.apache.commons.lang3.StringUtils;

public class RetailFactAgg {

    private final RetailFactEntity retailFact;
    private final TemplateEntity template;
    private final StoEntity store;
    private final SendSmsEntity sendSms;
    private final String errMsg;
    private final String errCode;

    RetailFactAgg(RetailFactEntity retailFact) {
        this.retailFact = retailFact;
        this.template = null;
        this.store = null;
        this.sendSms = null;
        this.errCode = "0001";
        this.errMsg = String.format("门店匹配失败:storeId=%s,companyId=%s", retailFact.getStoreId(), retailFact.getCompanyId());
    }

    RetailFactAgg(RetailFactEntity retailFact, StoEntity store) {
        this.retailFact = retailFact;
        this.template = null;
        this.store = store;
        this.sendSms = null;
        this.errCode = "0002";
        this.errMsg = String.format("门店 storeId=%s 或者公司 companyId=%s 尚未配置模板", store.getId(), store.getCompanyId());
    }

    RetailFactAgg(RetailFactEntity retailFact, StoEntity store, TemplateEntity template) {
        this.retailFact = retailFact;
        this.template = template;
        this.store = store;
        this.sendSms = null;
        this.errCode = "0003";
        this.errMsg = String.format("模板替换异常 template=%s", template.getContent());
    }

    RetailFactAgg(RetailFactEntity retailFact, StoEntity store, TemplateEntity template, String realContent, String errMsg) {
        this.retailFact = retailFact;
        this.template = template;
        this.store = store;
        this.sendSms = SendSmsEntity.createSmsByStore(realContent, retailFact.getPhone().orElse(null),
                retailFact.getVipName().orElse(""), store, BusinessType.CUSTOM_CARE, "", errMsg);
        this.errCode = "0004";
        this.errMsg = errMsg;
    }

    public RetailFactAgg(RetailFactEntity retailFact, TemplateEntity template, StoEntity store, String realContent) {
        this.retailFact = retailFact;
        this.template = template;
        this.store = store;
        this.sendSms = SendSmsEntity.createSmsByStore(realContent, retailFact.getPhone().orElse(null),
                retailFact.getVipName().orElse(""), store, BusinessType.CUSTOM_CARE, "", null);
        this.errMsg = null;
        this.errCode = "0000";
    }

    boolean hasSms() {
        return StringUtils.equals("0000", this.errCode) || StringUtils.equals("0004", this.errCode);
    }

    SendSmsEntity getSendSms() {
        return sendSms;
    }

    ReactorLogEntity createReactorLog() {
        Long templateId = template == null ? 0L : template.getId();
        String beforeCtx = template == null ? null : template.getContent();
        String afterCtx = sendSms == null ? null : sendSms.getContent();
        return new ReactorLogEntity(retailFact.getCompanyId(), retailFact.getStoreId(), 0, String.valueOf(retailFact.getId()),
                "acp.crm_retail_table", this.errCode, this.errMsg, beforeCtx, afterCtx, templateId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("retailFact", retailFact.getId())
                .add("errCode", errCode)
                .add("template", template == null)
                .add("store", store == null)
                .add("sendSms", sendSms == null)
                .add("errMsg", errMsg)
                .toString();
    }
}
