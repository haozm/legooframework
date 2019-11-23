package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.membercare.entity.BusinessType;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;
import java.util.Optional;

public class SendMessage implements Cloneable {

    private final Integer memberId, detailId;
    private final BusinessType businessType;
    private final String ctxTemplate;
    private String memberName, mobile, weixinId, deviceId, context;
    private boolean wxExits = false;
    private AutoRunChannel autoRunChannel;
    private boolean error = false;
    private String remark;
    private SendMode sendMode;

    // 添加其他的 replace 值
    private Map<String, Object> replaceMap;

    public String getRemark() {
        return remark;
    }

    public Optional<Map<String, Object>> getReplaceMap() {
        return Optional.ofNullable(MapUtils.isEmpty(replaceMap) ? null : replaceMap);
    }

    public void setReplaceMap(Map<String, Object> replaceMap) {
        if (this.replaceMap == null) this.replaceMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(replaceMap))
            this.replaceMap.putAll(replaceMap);
    }

    public boolean hasMemberId() {
        return this.memberId != 0;
    }

    public SendMessage createWithJobWithTemplate(BusinessType businessType, int detailId, int memberId,
                                                 AutoRunChannel runChannel, String ctxTemplate) {
        Preconditions.checkArgument(detailId > 0, "非法的任务ID=%s", detailId);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ctxTemplate), "消息模板不可以为空值...");
        return new SendMessage(businessType, detailId, memberId, runChannel, ctxTemplate);
    }

    public static SendMessage createWithoutJobWithTemplate(BusinessType businessType, int memberId,
                                                           AutoRunChannel runChannel, String ctxTemplate) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ctxTemplate), "消息模板不可以为空值...");
        return new SendMessage(businessType, 0, memberId, runChannel, ctxTemplate);
    }

    public SendMessage createWithJobNoTemplate(BusinessType businessType, int detailId, int memberId,
                                               AutoRunChannel runChannel) {
        Preconditions.checkArgument(detailId > 0, "非法的任务ID=%s", detailId);
        return new SendMessage(businessType, detailId, memberId, runChannel, null);
    }

    public static SendMessage createWithoutJobNoTemplate(BusinessType businessType, int memberId,
                                                         AutoRunChannel runChannel) {
        return new SendMessage(businessType, 0, memberId, runChannel, null);
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setError(String remark) {
        this.error = true;
        this.remark = remark;
    }

    boolean hasLegalPhone() {
        return StringUtils.isNotEmpty(this.mobile) && this.mobile.length() == 11 && NumberUtils.isDigits(this.mobile);
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    private SendMessage(BusinessType businessType, int detailId, int memberId, AutoRunChannel autoRunChannel,
                        String ctxTemplate) {
        Preconditions.checkArgument(memberId >= 0, "非法的会员ID %d", memberId);
        Preconditions.checkNotNull(autoRunChannel, "错误的入参 autoRunChannel");
        Preconditions.checkNotNull(businessType, "错误的入参 businessType");
        this.memberId = memberId;
        this.detailId = detailId;
        this.businessType = businessType;
        this.autoRunChannel = autoRunChannel;
        this.ctxTemplate = ctxTemplate;
    }

    public boolean isOK() {
        return !this.error;
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

    public SendMessage changeMobile(String mobile) {
        try {
            SendMessage clone = (SendMessage) this.clone();
            clone.mobile = mobile;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("memberId", memberId)
                .add("detailId", detailId)
                .add("businessType", businessType)
                .add("ctxTemplate", ctxTemplate)
                .add("memberName", memberName)
                .add("mobile", mobile)
                .add("weixinId", weixinId)
                .add("deviceId", deviceId)
                .add("context", context)
                .add("wxExits", wxExits)
                .add("autoRunChannel", autoRunChannel)
                .add("error", error)
                .add("remark", remark)
                .add("replaceMap", replaceMap)
                .toString();
    }
}
