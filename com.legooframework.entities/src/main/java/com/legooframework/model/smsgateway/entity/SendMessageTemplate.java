package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.membercare.entity.BusinessType;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class SendMessageTemplate implements Cloneable {

    private final Integer memberId, detailId;
    private final BusinessType businessType;
    private final String ctxTemplate;
    private String memberName, mobile, weixinId, deviceId, context, resulat;
    private boolean wxExits = false;
    private AutoRunChannel autoRunChannel;
    private boolean error = false;
    private String errmsg;

    public SendMessageTemplate createWithJobWithTemplate(BusinessType businessType, int detailId, int memberId,
                                                         AutoRunChannel runChannel, String ctxTemplate) {
        Preconditions.checkArgument(detailId > 0, "非法的任务ID=%s", detailId);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ctxTemplate), "消息模板不可以为空值...");
        return new SendMessageTemplate(businessType, detailId, memberId, runChannel, ctxTemplate);
    }

    public SendMessageTemplate createWithoutJobWithTemplate(BusinessType businessType, int memberId,
                                                            AutoRunChannel runChannel, String ctxTemplate) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ctxTemplate), "消息模板不可以为空值...");
        return new SendMessageTemplate(businessType, 0, memberId, runChannel, ctxTemplate);
    }

    public SendMessageTemplate createWithJobNoTemplate(BusinessType businessType, int detailId, int memberId,
                                                       AutoRunChannel runChannel) {
        Preconditions.checkArgument(detailId > 0, "非法的任务ID=%s", detailId);
        return new SendMessageTemplate(businessType, detailId, memberId, runChannel, null);
    }

    public SendMessageTemplate createWithoutJobNoTemplate(BusinessType businessType, int memberId, AutoRunChannel runChannel) {
        return new SendMessageTemplate(businessType, 0, memberId, runChannel, null);
    }

    public void setError(String errmsg) {
        this.error = true;
        this.errmsg = errmsg;
    }

    private SendMessageTemplate(BusinessType businessType, int detailId, int memberId, AutoRunChannel autoRunChannel,
                                String ctxTemplate) {
        Preconditions.checkArgument(memberId > 0, "非法的会员ID %d", memberId);
        Preconditions.checkNotNull(autoRunChannel, "错误的入参 autoRunChannel");
        Preconditions.checkNotNull(businessType, "错误的入参 businessType");
        this.memberId = memberId;
        this.detailId = detailId;
        this.businessType = businessType;
        this.autoRunChannel = autoRunChannel;
        this.ctxTemplate = ctxTemplate;
    }

    boolean hasCtxTemplate() {
        return !Strings.isNullOrEmpty(this.ctxTemplate);
    }

    public Optional<String> getCtxTemplate() {
        return Optional.ofNullable(ctxTemplate);
    }

    AutoRunChannel getAutoRunChannel() {
        return autoRunChannel;
    }

    public boolean hasSms() {
        return AutoRunChannel.WX_THEN_SMS == autoRunChannel || AutoRunChannel.WX_AND_SMS == autoRunChannel ||
                AutoRunChannel.WX_ONLY == autoRunChannel;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setContext(String context) {
        if (Strings.isNullOrEmpty(this.context))
            this.context = context;
    }


    public void setMemberInfo(String mobile, String memberName) {
        if (Strings.isNullOrEmpty(this.memberName))
            this.memberName = memberName;
        if (Strings.isNullOrEmpty(this.mobile))
            this.mobile = mobile;
    }

    public void setResulat(String resulat) {
        this.resulat = resulat;
    }

    public void setAutoRunChannel(AutoRunChannel autoRunChannel) {
        this.autoRunChannel = autoRunChannel;
    }

    public void setWeixinInfo(String weixinId, String deviceId) {
        if (!Strings.isNullOrEmpty(weixinId) && !Strings.isNullOrEmpty(deviceId)) {
            this.weixinId = weixinId;
            this.deviceId = deviceId;
            this.wxExits = true;
        }
    }


    public Integer getDetailId() {
        return detailId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getWeixinId() {
        return weixinId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isWxExits() {
        return wxExits;
    }

    public String getContext() {
        return context;
    }

    public SendMessageTemplate changeMobile(String mobile) {
        try {
            SendMessageTemplate clone = (SendMessageTemplate) this.clone();
            clone.mobile = mobile;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isOK() {
        return StringUtils.equals("OK", this.resulat);
    }

    @Override
    public String toString() {
        // detailId|memberId|sendChanel|weixinId@deviceId|mobile|{encoding}memberName|{encoding}context|resulat
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", detailId, memberId, autoRunChannel.getChannel(),
                this.wxExits ? String.format("%s@%s", weixinId, deviceId) : "NULL",
                Strings.isNullOrEmpty(mobile) ? "0000" : mobile,
                Strings.isNullOrEmpty(memberName) ? "NULL" : WebUtils.encodeUrl(memberName),
                Strings.isNullOrEmpty(context) ? "NULL" : WebUtils.encodeUrl(context),
                Strings.isNullOrEmpty(resulat) ? "NOTEXITS" : resulat);
    }
}
