package com.legooframework.model.smsgateway.entity;

import com.google.common.base.*;
import org.apache.commons.lang3.StringUtils;

public class SMSEntity {

    private String content, phoneNo, smsId, memberName;
    private int wordCount, smsNum;
    private Integer memberId;

    private SMSEntity(String originalId, String content, String phoneNo, Integer memberId, String memberName) {
        this.content = content;
        setPhoneNo(phoneNo);
        this.smsId = originalId;
        this.wordCount = content.length();
        this.smsNum = this.wordCount <= 70 ? 1 : 2;
        this.memberId = memberId;
        setMemberName(memberName);
    }

    public static SMSEntity createSMS(String originalId, String content, String phoneNo, Integer memberId, String memberName) {
        return new SMSEntity(originalId, content, phoneNo, memberId, memberName);
    }

    // 构造4DB
    SMSEntity(String content, String phoneNo, String smsId, int wordCount, int smsNum, Integer memberId, String memberName) {
        this.content = content;
        this.phoneNo = phoneNo;
        this.smsId = smsId;
        this.wordCount = wordCount;
        this.smsNum = smsNum;
        this.memberId = memberId;
        this.memberName = memberName;
    }

    private void setMemberName(String memberName) {
        if (Strings.isNullOrEmpty(memberName)) {
            this.memberName = null;
        } else {
            String _memberName = CharMatcher.whitespace().removeFrom(Strings.nullToEmpty(memberName));
            this.memberName = Strings.isNullOrEmpty(_memberName) ? null : memberName;
        }
    }

    private void setPhoneNo(String phoneNo) {
        String _phone = CharMatcher.whitespace().removeFrom(Strings.nullToEmpty(phoneNo));
        this.phoneNo = Strings.isNullOrEmpty(_phone) ? "0" : _phone;
    }

    public void addPrefix(String prefix) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "短信前缀不可以为空值...");
        String _prefix = String.format("【%s】", prefix);
        if (StringUtils.startsWith(this.content, _prefix)) return;
        this.content = String.format("【%s】%s", prefix, content);
        this.wordCount = content.length();
        this.smsNum = this.wordCount <= 70 ? 1 : 2;
    }

    public void addPrefixAndSuffix(String prefix) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "短信前缀不可以为空值...");
        if (StringUtils.endsWith(this.content, "回复TD退订")) return;
        this.content = String.format("【%s】%s%s", prefix, content, "回复TD退订");
        this.wordCount = content.length();
        this.smsNum = this.wordCount <= 70 ? 1 : 2;
    }

    public String getMemberName() {
        return memberName;
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
                Objects.equal(phoneNo, smsEntity.phoneNo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(smsId, content, phoneNo);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("smsId", smsId)
                .add("content", content)
                .add("phoneNo", phoneNo)
                .add("wordCount", wordCount)
                .add("smsNum", smsNum)
                .toString();
    }
}
