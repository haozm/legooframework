package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.membercare.entity.AutoRunChannel;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.SendMode;

import java.util.List;
import java.util.Optional;

public class SendMessageDto {

    private final Integer companyId, storeId, employeeId;
    private final boolean authorization, end;
    private final String template, batchNo;
    private final SendMode sendMode;
    private final BusinessType businessType;
    private final List<String> payloads;

    SendMessageDto(Integer companyId, Integer storeId, Integer employeeId, boolean authorization, boolean end,
                   String template, String batchNo, SendMode sendMode, BusinessType businessType, List<String> payloads) {
        this.companyId = companyId;
        this.storeId = storeId;
        this.employeeId = employeeId;
        this.authorization = authorization;
        this.end = end;
        this.template = template;
        this.batchNo = batchNo;
        this.sendMode = sendMode;
        this.businessType = businessType;
        this.payloads = payloads;

    }

    public SendMessageDto(SendMessageDto sendMessageDto, List<String> payloads) {
        this.companyId = sendMessageDto.companyId;
        this.storeId = sendMessageDto.storeId;
        this.employeeId = sendMessageDto.employeeId;
        this.authorization = sendMessageDto.authorization;
        this.end = sendMessageDto.end;
        this.template = sendMessageDto.template;
        this.batchNo = sendMessageDto.batchNo;
        this.sendMode = sendMessageDto.sendMode;
        this.businessType = sendMessageDto.businessType;
        this.payloads = payloads;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public boolean isAuthorization() {
        return authorization;
    }

    public boolean isEnd() {
        return end;
    }

    public List<String> getPayloads() {
        return payloads;
    }

    public Optional<String> getTemplate() {
        return Optional.ofNullable(template);
    }

    public String getBatchNo() {
        return batchNo;
    }

    public SendMode getSendMode() {
        return sendMode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("employeeId", employeeId)
                .add("authorization", authorization)
                .add("template", template)
                .add("batchNo", batchNo)
                .add("sendMode", sendMode)
                .add("businessType", businessType)
                .add("end", end)
                .add("payloads.size", payloads.size())
                .toString();
    }
}
