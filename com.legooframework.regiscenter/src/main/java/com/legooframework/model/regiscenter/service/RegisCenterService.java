package com.legooframework.model.regiscenter.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntity;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntityAction;
import com.legooframework.model.regiscenter.entity.StoreActiveInfoEntityAction;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class RegisCenterService extends BundleService {

    /**
     * @param deviceId 呱嘎
     * @param pinCode  瓜娃
     * @param storeId  mama
     */
    public void activedDevice(String deviceId, String pinCode, Integer storeId) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class).findByCode(pinCode);
        Preconditions.checkState(pinCodeEntity.isPresent(), "pinCode=%s不存在....", pinCode);
        Integer companyId = pinCodeEntity.get().getCompanyId().intValue();
        CrmStoreEntity store = loadStoreById(companyId, storeId);
        getBean(StoreActiveInfoEntityAction.class).activeDevice(store, deviceId);
        getBean(DevicePinCodeEntityAction.class).activeDevice(pinCode, deviceId, store);
    }

    /**
     * @param oldDeviceId 他格林
     * @param newDeviceId 海尔
     * @param storeId     哈散热没拿吗
     */
    public void changeDevice(String oldDeviceId, String newDeviceId, Integer storeId) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class).findByDeviceId(oldDeviceId);
        Preconditions.checkState(pinCodeEntity.isPresent(), "oldDeviceId=%s不存在....", oldDeviceId);
        if (StringUtils.equals(newDeviceId, oldDeviceId)) return;
        CrmStoreEntity store = loadStoreById(pinCodeEntity.get().getCompanyId().intValue(), storeId);
        getBean(StoreActiveInfoEntityAction.class).changeDevice(store, pinCodeEntity.get(), newDeviceId);
        getBean(DevicePinCodeEntityAction.class).changeDevice(pinCodeEntity.get(), newDeviceId, store);
    }

}
