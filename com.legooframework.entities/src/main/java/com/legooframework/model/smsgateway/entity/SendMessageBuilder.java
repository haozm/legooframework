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

public class SendMessageBuilder {

    private Integer memberId;
    private Long detailId;
    private final BusinessType businessType;
    private String ctxTemplate, memberName, mobile, weixinId, deviceId, context, message;
    private AutoRunChannel autoRunChannel;
    private boolean error = false;
    private SendMode sendMode;
    private Map<String, Object> replaceMap;

    private SendMessageBuilder(BusinessType businessType) {
        this.businessType = businessType;
        this.replaceMap = Maps.newHashMap();
    }

    public void changeMobile(String mobile) {
        this.mobile = mobile;
    }

    AutoRunChannel getAutoRunChannel() {
        Preconditions.checkNotNull(this.autoRunChannel, "尚未初始化，获取该值失败...");
        return autoRunChannel;
    }

    public static SendMessageBuilder createMessageBuilder(BusinessType businessType) {
        return new SendMessageBuilder(businessType);
    }

    boolean hasWechat() {
        return !Strings.isNullOrEmpty(weixinId) && !Strings.isNullOrEmpty(deviceId);
    }

    boolean hasLegalPhone() {
        return StringUtils.isNotEmpty(this.mobile) && this.mobile.length() == 11 && NumberUtils.isDigits(this.mobile);
    }

    public long getDetailId() {
        return detailId == null ? 0L : detailId;
    }

    public int getIntDetailId() {
        return detailId == null ? 0 : detailId.intValue();
    }

    public BusinessType getBusinessType() {
        return businessType;
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

    public String getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }

    public SendMode getSendMode() {
        return sendMode;
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

    public SendMessageBuilder withMember(String mobile, String memberName) {
        this.mobile = mobile;
        this.memberName = memberName;
        return this;
    }

    public boolean hasMemberId() {
        return this.memberId != null && this.memberId > 0;
    }

    public Integer getMemberId() {
        Preconditions.checkState(hasMemberId(), "当前memberId 尚未赋值，获取失败...");
        return memberId;
    }

    public Optional<Integer> getOptMemberId() {
        return Optional.ofNullable(hasMemberId() ? memberId : null);
    }

    public SendMessageBuilder withAutoRunChannel(AutoRunChannel autoRunChannel) {
        this.autoRunChannel = autoRunChannel;
        return this;
    }

    public SendMessageBuilder withCtxTemplate(String ctxTemplate) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ctxTemplate), "消息模板不可以为空值...");
        this.ctxTemplate = ctxTemplate;
        return this;
    }

    public Optional<Map<String, Object>> getReplaceMap() {
        return Optional.ofNullable(MapUtils.isEmpty(replaceMap) ? null : replaceMap);
    }

    public SendMessageBuilder withContext(String context) {
        this.context = context;
        return this;
    }

    public Optional<String> getCtxTemplate() {
        return Optional.ofNullable(ctxTemplate);
    }

    public SendMessageBuilder withReplaceParam(String key, Object value) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        this.replaceMap.put(key, value);
        return this;
    }

    public SendMessageBuilder withReplaceMap(Map<String, Object> replaceMap) {
        if (MapUtils.isNotEmpty(replaceMap))
            this.replaceMap.putAll(replaceMap);
        return this;
    }


    public SendMessageBuilder withSendMode(SendMode sendMode) {
        this.sendMode = sendMode;
        return this;
    }

    public SendMessageBuilder withJobId(Long jobId) {
        Preconditions.checkArgument(jobId > 0, "非法的任务ID=%s", detailId);
        this.detailId = jobId;
        return this;
    }

    public SendMessageBuilder withError(String message) {
        this.error = true;
        this.message = message;
        return this;
    }

    public boolean isError() {
        return error;
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
                .add("autoRunChannel", autoRunChannel)
                .add("error", error)
                .add("message", message)
                .add("sendMode", sendMode)
                .add("replaceMap", replaceMap)
                .toString();
    }
}
