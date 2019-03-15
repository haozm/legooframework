package com.legooframework.model.regiscenter.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntity;
import com.legooframework.model.regiscenter.entity.DevicePinCodeEntityAction;
import com.legooframework.model.regiscenter.entity.StoreActiveInfoEntityAction;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class RegisCenterService extends RegBaseService {

    public void activedDevice(String deviceId, String pinCode, Integer storeId) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class).findByCode(pinCode);
        Preconditions.checkState(pinCodeEntity.isPresent(), "pinCode=%s不存在....", pinCode);
        Integer companyId = pinCodeEntity.get().getCompanyId().intValue();
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s对应的公司不存在...", companyId);
        Optional<CrmStoreEntity> store = getBean(CrmStoreEntityAction.class).findById(company.get(), storeId);
        Preconditions.checkState(store.isPresent(), "ID=%s 对应的门店不存在...", storeId);
        getBean(StoreActiveInfoEntityAction.class).activeDevice(store.get(), deviceId);
        getBean(DevicePinCodeEntityAction.class).activeDevice(pinCode, deviceId, store.get());
    }


    public void changeDevice(String oldDeviceId, String newDeviceId, Integer storeId) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<DevicePinCodeEntity> pinCodeEntity = getBean(DevicePinCodeEntityAction.class).findByDeviceId(oldDeviceId);
        Preconditions.checkState(pinCodeEntity.isPresent(), "oldDeviceId=%s不存在....", oldDeviceId);
        if (StringUtils.equals(newDeviceId, oldDeviceId)) return;
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(pinCodeEntity.get().getCompanyId().intValue());
        Preconditions.checkState(company.isPresent(), "ID=%s对应的公司不存在...", pinCodeEntity.get().getCompanyId());

        Optional<CrmStoreEntity> store = getBean(CrmStoreEntityAction.class).findById(company.get(), storeId);
        Preconditions.checkState(store.isPresent(), "ID=%s 对应的门店不存在...", storeId);

        getBean(StoreActiveInfoEntityAction.class).changeDevice(store.get(), pinCodeEntity.get(), newDeviceId);
        getBean(DevicePinCodeEntityAction.class).changeDevice(pinCodeEntity.get(), newDeviceId, store.get());
    }

}
