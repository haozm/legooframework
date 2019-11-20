package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.commons.entity.SendChannel;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.membercare.entity.BusinessType;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SMSEntity {

    private final String smsId, phoneNo, memberName;
    private String content;
    private int wordCount, smsNum;
    private final Integer memberId;
    private Integer jobId;
    private boolean enbaled;
    private final SendChannel sendChannel;
    private final String weixinId, deviceId;
    private String remark;

    private BusinessType businessType;

    private SMSEntity(String originalId, String content, String phoneNo, Integer memberId, String memberName,
                      Integer jobId, boolean enbaled, String remark, BusinessType businessType) {
        this.content = content;
        this.smsId = originalId;
        this.wordCount = content.length();
        this.smsNum = this.wordCount <= 70 ? 1 : (this.wordCount % 67 + 1);
        this.memberId = memberId;
        this.memberName = memberName;
        this.phoneNo = phoneNo;
        this.jobId = jobId;
        this.enbaled = enbaled;
        this.sendChannel = SendChannel.SMS;
        this.weixinId = null;
        this.deviceId = null;
        this.remark = remark;
        this.businessType = businessType;
    }

    private SMSEntity(String originalId, String content, Integer memberId, String memberName, Integer jobId,
                      String weixinId, String deviceId, boolean enbaled, String remark, BusinessType businessType) {
        this.content = content;
        this.smsId = originalId;
        this.wordCount = content.length();
        this.smsNum = this.wordCount <= 70 ? 1 : (this.wordCount % 67 + 1);
        this.memberId = memberId;
        this.memberName = memberName;
        this.phoneNo = null;
        this.jobId = jobId;
        this.enbaled = enbaled;
        this.sendChannel = SendChannel.WEIXIN;
        this.weixinId = weixinId;
        this.deviceId = deviceId;
        this.remark = remark;
        this.businessType = businessType;
    }

//    public static SMSEntity createSMSMsgWithJob(String smsId, Integer memberId, String phoneNo,
//                                                String memberName, String content, Integer jobId) {
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(StringUtils.trimToNull(content)), "短信内容为空,创建短信失败....");
//        return new SMSEntity(smsId, content, phoneNo, memberId, memberName, jobId, true);
//    }

    public static SMSEntity createSMSMsgWithNoJob(String smsId, Integer memberId, String phoneNo, String memberName,
                                                  String content, BusinessType businessType) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(StringUtils.trimToNull(content)), "短信内容为空,创建短信失败....");
        return new SMSEntity(smsId, content, phoneNo, memberId, memberName, 0, true, null, businessType);
    }

    public static List<SMSEntity> createSMSMsg(SendMessageTemplate msgTmp) {
        List<SMSEntity> list = Lists.newArrayListWithCapacity(2);
        if (msgTmp.getAutoRunChannel() == AutoRunChannel.SMS_ONLY) {
            list.add(createSms(msgTmp));
        } else if (msgTmp.getAutoRunChannel() == AutoRunChannel.WX_ONLY) {
            createWx(msgTmp).ifPresent(list::add);
        } else if (msgTmp.getAutoRunChannel() == AutoRunChannel.WX_THEN_SMS) {
            if (msgTmp.isWxExits()) {
                createWx(msgTmp).ifPresent(list::add);
            } else {
                createSms(msgTmp);
            }
        } else {
            list.add(createSms(msgTmp));
            createWx(msgTmp).ifPresent(list::add);
        }
        return list;
    }

    private static Optional<SMSEntity> createWx(SendMessageTemplate msgTmp) {
        if (msgTmp.isWxExits()) {
            SMSEntity res = new SMSEntity(UUID.randomUUID().toString(), msgTmp.getContext(), msgTmp.getMemberId(),
                    msgTmp.getMemberName(), msgTmp.getDetailId(), msgTmp.getWeixinId(), msgTmp.getDeviceId(), msgTmp.isOK(),
                    msgTmp.getRemark(), msgTmp.getBusinessType());
            return Optional.of(res);
        }
        return Optional.empty();
    }

    private static SMSEntity createSms(SendMessageTemplate msgTmp) {
        if (msgTmp.isOK()) {
            if (msgTmp.hasLegalPhone()) {
                return new SMSEntity(UUID.randomUUID().toString(), msgTmp.getContext(), msgTmp.getMobile(),
                        msgTmp.getMemberId(), msgTmp.getMemberName(), msgTmp.getDetailId(),
                        msgTmp.isOK(), msgTmp.getRemark(), msgTmp.getBusinessType());
            } else {
                return new SMSEntity(UUID.randomUUID().toString(), msgTmp.getContext(), msgTmp.getMobile(),
                        msgTmp.getMemberId(), msgTmp.getMemberName(), msgTmp.getDetailId(), false,
                        "非法的移动电话号码...", msgTmp.getBusinessType());
            }
        } else {
            return new SMSEntity(UUID.randomUUID().toString(), msgTmp.getContext(), msgTmp.getMobile(),
                    msgTmp.getMemberId(), msgTmp.getMemberName(), msgTmp.getDetailId(), false,
                    msgTmp.getRemark(), msgTmp.getBusinessType());
        }
    }

    String getRemark() {
        return remark;
    }

    // 构造4DB
    private SMSEntity(ResultSet res) {
        try {
            this.content = ResultSetUtil.getString(res, "smsContext");
            this.phoneNo = ResultSetUtil.getOptString(res, "phoneNum", null);
            this.smsId = ResultSetUtil.getString(res, "id");
            this.wordCount = ResultSetUtil.getObject(res, "wordCount", Integer.class);
            this.smsNum = ResultSetUtil.getObject(res, "smsCount", Integer.class);
            this.memberId = ResultSetUtil.getOptObject(res, "memberId", Integer.class).orElse(-1);
            this.memberName = ResultSetUtil.getOptString(res, "memberName", null);
            this.jobId = ResultSetUtil.getOptObject(res, "jobId", Integer.class).orElse(null);
            this.enbaled = ResultSetUtil.getBooleanByInt(res, "enabled");
            this.sendChannel = SendChannel.paras(res.getInt("sendChannel"));
            this.weixinId = ResultSetUtil.getOptString(res, "weixinId", null);
            this.deviceId = ResultSetUtil.getOptString(res, "deviceId", null);
            this.remark = ResultSetUtil.getOptString(res, "remarks", null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSEntity has SQLException", e);
        }
    }

    boolean isSMSMsg() {
        return SendChannel.SMS == this.sendChannel;
    }

    boolean isWxMsg() {
        return SendChannel.WEIXIN == this.sendChannel;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    static SMSEntity createInstance(ResultSet res) {
        return new SMSEntity(res);
    }

    Integer getJobId() {
        return jobId;
    }

    // 构造函数 4 发送服务器
    private SMSEntity(String smsId, String content, String phoneNo, int wordCount, int smsNum) {
        this.content = content;
        this.phoneNo = phoneNo;
        this.smsId = smsId;
        this.wordCount = wordCount;
        this.smsNum = smsNum;
        this.memberId = null;
        this.memberName = null;
        this.enbaled = true;
        this.sendChannel = SendChannel.SMS;
        this.weixinId = null;
        this.deviceId = null;
    }

    boolean isEnbaled() {
        return enbaled;
    }

    public static SMSEntity create4Sending(String smsId, String content, String phoneNo, int wordCount, int smsNum) {
        return new SMSEntity(smsId, content, phoneNo, wordCount, smsNum);
    }

    // 构造4DB
    SMSEntity(String smsId, int smsNum) {
        this.content = null;
        this.phoneNo = null;
        this.smsId = smsId;
        this.wordCount = 0;
        this.smsNum = smsNum;
        this.memberId = null;
        this.memberName = null;
        this.enbaled = true;
        this.sendChannel = SendChannel.SMS;
        this.deviceId = null;
        this.weixinId = null;
    }

    public void addPrefix(String prefix) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "短信前缀不可以为空值...");
        String _prefix = String.format("【%s】", prefix);
        if (StringUtils.startsWith(this.content, _prefix)) return;
        this.content = String.format("【%s】%s", prefix, content);
        this.wordCount = content.length();
        this.smsNum = this.wordCount <= 70 ? 1 : (this.wordCount % 67 + 1);
    }

    public void addPrefixAndSuffix(String prefix, String suffix) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "短信前缀不可以为空值...");
        String _prefix = String.format("【%s】", prefix);
        if (StringUtils.startsWith(this.content, _prefix) && !StringUtils.endsWith(this.content, suffix)) {
            this.content = String.format("%s%s", content, suffix);
        } else if (!StringUtils.startsWith(this.content, _prefix) && StringUtils.endsWith(this.content, suffix)) {
            this.content = String.format("%s%s", _prefix, content);
        } else if (!StringUtils.startsWith(this.content, _prefix) && !StringUtils.endsWith(this.content, suffix)) {
            this.content = String.format("【%s】%s%s", prefix, content, suffix);
        }
        this.wordCount = content.length();
        this.smsNum = this.wordCount <= 70 ? 1 : (this.wordCount % 67 + 1);
    }

    String getEncodeCtx() {
        return WebUtils.encodeUrl(this.content);
    }

    String getMemberName() {
        return memberName;
    }

    SendChannel getSendChannel() {
        return sendChannel;
    }

    public Optional<String> getWeixinIdIfExists() {
        return Optional.ofNullable(weixinId);
    }

    public Integer getMemberId() {
        return memberId;
    }

    public String getSmsId() {
        return smsId;
    }

    public String getContent() {
        return content;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public int getWordCount() {
        return wordCount;
    }

    public String getWeixinId() {
        return weixinId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getSmsNum() {
        return smsNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSEntity)) return false;
        SMSEntity smsEntity = (SMSEntity) o;
        return Objects.equal(content, smsEntity.content) &&
                Objects.equal(smsId, smsEntity.smsId) &&
                Objects.equal(weixinId, smsEntity.weixinId) &&
                Objects.equal(deviceId, smsEntity.deviceId) &&
                Objects.equal(sendChannel, smsEntity.sendChannel) &&
                Objects.equal(phoneNo, smsEntity.phoneNo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(smsId, content, phoneNo, sendChannel, weixinId, deviceId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("smsId", smsId)
                .add("phoneNo", phoneNo)
                .add("wordCount", wordCount)
                .add("smsNum", smsNum)
                .add("content", content)
                .add("weixinId", weixinId)
                .add("deviceId", deviceId)
                .add("communicationChannel", sendChannel)
                .toString();
    }
}
