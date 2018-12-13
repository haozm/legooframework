package com.legooframework.model.regiscenter.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntity;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntityAction;
import com.legooframework.model.regiscenter.event.RegisCenterEventFactory;

import java.util.Optional;

public class RegisCenterService extends RegBaseService {

    public boolean activedDeviceByPinCode(String deviceId, String pinCode, String imei1, String imei2) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class).findByCode(pinCode);
        Preconditions.checkState(pinCodeEntity.isPresent(), "pinCode=%s不存在....", pinCode);
        boolean res = getBean(DevicePinCodeEntityAction.class).activeDeviceId(pinCode, deviceId);
        getAsycEventBus().postEvent(RegisCenterEventFactory.activeDeviceByPinCodeEvent(deviceId, imei1, imei2,
                pinCodeEntity.get().getCompanyId()));
        return res;
    }

}
