package com.legooframework.model.smsprovider.entity;

import com.google.common.base.MoreObjects;

public class SmsStateDto {
    private final String smsId, stateDate, stateDesc;
    private final int stateCode;

    SmsStateDto(String smsId, int stateCode, String stateDate, String stateDesc) {
        this.smsId = smsId;
        this.stateDate = stateDate;
        this.stateDesc = stateDesc;
        this.stateCode = stateCode;
    }

    public String getSmsId() {
        return smsId;
    }

    public String getStateDate() {
        return stateDate;
    }

    public int getStateCode() {
        return stateCode;
    }

    public String getStateDesc() {
        return stateDesc;
    }

    public boolean hasError() {
        return this.stateCode == 9;
    }

    public boolean hasFinalState() {
        return this.stateCode == 1 || this.stateCode == 2;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("smsId", smsId)
                .add("stateDate", stateDate)
                .add("stateDesc", stateDesc)
                .add("stateCode", stateCode)
                .toString();
    }
}
