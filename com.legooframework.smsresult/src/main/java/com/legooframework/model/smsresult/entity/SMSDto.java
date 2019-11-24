package com.legooframework.model.smsresult.entity;

import com.google.common.base.MoreObjects;

public class SMSDto {

    private final String smsId, content, phoneNo;
    private final int wordCount, smsNum;

    public SMSDto(String smsId, String content, String phoneNo, int wordCount, int smsNum) {
        this.content = content;
        this.phoneNo = phoneNo;
        this.smsId = smsId;
        this.wordCount = wordCount;
        this.smsNum = smsNum;
    }


    String getSmsId() {
        return smsId;
    }

    String getContent() {
        return content;
    }

    String getPhoneNo() {
        return phoneNo;
    }

    int getWordCount() {
        return wordCount;
    }

    int getSmsNum() {
        return smsNum;
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
