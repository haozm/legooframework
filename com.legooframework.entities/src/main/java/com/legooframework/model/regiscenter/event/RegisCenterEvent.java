package com.legooframework.model.regiscenter.event;

import com.legooframework.model.core.event.LegooEvent;

public class RegisCenterEvent extends LegooEvent {

    RegisCenterEvent(String eventName) {
        super("regisCenter", eventName);
    }

    public String getDeviceId() {
        return super.getString("deviceId");
    }

    public Long getCompanyId() {
        return super.getNullAbleValue("companyId", Long.class);
    }

    void setDeviceId(String deviceId) {
        super.putPayload("deviceId", deviceId);
    }

    void setCompanyId(Long companyId) {
        super.putPayload("companyId", companyId);
    }

    void setImei1(String imei1) {
        super.putPayload("imei1", imei1);
    }

    public String getImei1() {
        return super.getString("imei1");
    }

    void setImei2(String imei2) {
        super.putPayload("imei2", imei2);
    }

    public String getImei2() {
        return super.getNullAbleValue("imei2", String.class);
    }

}
