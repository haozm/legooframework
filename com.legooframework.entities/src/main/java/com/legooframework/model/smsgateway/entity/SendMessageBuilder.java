package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.membercare.entity.BusinessType;

public class SendMessageBuilder {

    private Integer memberId, detailId;
    private final BusinessType businessType;
    private String ctxTemplate;
    private String memberName, mobile, weixinId, deviceId, context;
    private AutoRunChannel autoRunChannel;
    private boolean error = false;
    private String remark;
    private SendMode sendMode;

    private SendMessageBuilder(BusinessType businessType) {
        this.businessType = businessType;
    }

    public static SendMessageBuilder createMessageBuilder(BusinessType businessType) {
        return new SendMessageBuilder(businessType);
    }

    public SendMessageBuilder withWechat(String weixinId, String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId));
        this.weixinId = weixinId;
        this.deviceId = deviceId;
        return this;
    }

    public SendMessageBuilder withMember(Integer memberId, String mobile, String memberName) {
        this.memberId = memberId;
        this.mobile = mobile;
        this.memberName = memberName;
        return this;
    }

    public SendMessageBuilder withAutoRunChannel(AutoRunChannel autoRunChannel) {
        this.autoRunChannel = autoRunChannel;
        return this;
    }

    public SendMessageBuilder withCtxTemplate(String ctxTemplate) {
        this.ctxTemplate = ctxTemplate;
        return this;
    }

    public SendMessageBuilder withContext(String context) {
        this.context = context;
        return this;
    }


    public SendMessageBuilder withSendMode(SendMode sendMode) {
        this.sendMode = sendMode;
        return this;
    }

    public SendMessageBuilder withJobId(Integer jobId) {
        this.detailId = jobId;
        return this;
    }
}
