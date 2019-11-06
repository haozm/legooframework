package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;

public class SendMessageTemplate implements Cloneable {

    private Integer memberId, detailId;
    private String memberName, mobile, weixinId, context, resulat, deviceId;
    private boolean wxExits = false;
    private AutoRunChannel autoRunChannel;

    private SendMessageTemplate(String baseInfo) {
        String[] args = StringUtils.split(baseInfo, ',');
        Preconditions.checkArgument(args.length == 3, "非法的报文%s", baseInfo);
        this.detailId = Integer.valueOf(args[0]);
        this.memberId = Integer.valueOf(args[1]);
        this.autoRunChannel = AutoRunChannel.parse(Integer.parseInt(args[2]));
        this.weixinId = null;
        this.deviceId = null;
    }

    private SendMessageTemplate(Integer detailId, Integer memberId, int autoRunChannel, String weixinInfo, String mobile,
                                String memberName, String context, String resulat) {
        this.memberId = memberId;
        this.detailId = detailId;
        this.memberName = memberName;
        this.autoRunChannel = AutoRunChannel.parse(autoRunChannel);
        this.mobile = mobile;
        this.context = context;
        this.resulat = resulat;
        if (!Strings.isNullOrEmpty(weixinInfo)) {
            String[] args = StringUtils.split(weixinInfo, '@');
            this.weixinId = args[0];
            this.deviceId = args[1];
            this.wxExits = true;
        }
    }

    public AutoRunChannel getAutoRunChannel() {
        return autoRunChannel;
    }

    public boolean hasSms() {
        return AutoRunChannel.WX_THEN_SMS == autoRunChannel || AutoRunChannel.WX_AND_SMS == autoRunChannel ||
                AutoRunChannel.WX_ONLY == autoRunChannel;
    }

    public static SendMessageTemplate createByMemberId(String baseInfo) {
        return new SendMessageTemplate(baseInfo);
    }

    public static SendMessageTemplate deCoding(String fullInfo) {
        String[] args = StringUtils.split(fullInfo, ',');
        return new SendMessageTemplate(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]),
                StringUtils.equals("NULL", args[3]) ? null : args[3],
                args[4], StringUtils.equals("NULL", args[5]) ? null : WebUtils.decodeUrl(args[5]),
                WebUtils.decodeUrl(args[6]), args[7]);
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setMobile(String mobile) {
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

    public void setContext(String context) {
        this.context = context;
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
