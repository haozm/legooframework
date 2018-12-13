package com.legooframework.model.organization.event;

import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.organization.entity.CompanyEntity;
import com.legooframework.model.organization.entity.EmployeeEntity;
import com.legooframework.model.organization.entity.StoreEntity;
import com.legooframework.model.security.entity.AccountEntity;
import org.apache.commons.collections4.MapUtils;

import java.util.List;

public class OrgModuleEvent extends LegooEvent {

    OrgModuleEvent(String source, String eventName) {
        super(source, "organization", eventName);
    }

    OrgModuleEvent(String eventName) {
        super("organization", eventName);
    }

    void setStore(StoreEntity store) {
        super.putPayload("store", store);
    }

    void setAccountEntity(AccountEntity account) {
        super.putPayload("AccountEntity", account);
    }

    public AccountEntity getAccountEntity() {
        return super.getNullAbleValue("AccountEntity", AccountEntity.class);
    }

    void setEquipmentId(String imie) {
        super.putPayload("equipmentId", imie);
    }

    public String getEquipmentId() {
        return MapUtils.getString(super.payload, "equipmentId");
    }

    void setStoreId(Long storeId) {
        super.putPayload("storeId", storeId);
    }

    void setCompanyId(Long companyId) {
        super.putPayload("companyId", companyId);
    }

    void setStoreIds(List<Long> storeIds) {
        super.putPayload("storeIds", storeIds);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getStoreIds() {
        return (List<Long>) MapUtils.getObject(super.payload, "storeIds");
    }

    public Long getCompanyId() {
        return MapUtils.getLong(super.payload, "companyId");
    }


    void setDeviceId(String deviceId) {
        super.putPayload("deviceId", deviceId);
    }

    public String getDeviceId() {
        return MapUtils.getString(super.payload, "deviceId");
    }

    public Long getStoreId() {
        return MapUtils.getLong(super.payload, "storeId");
    }

    void setCompany(CompanyEntity company) {
        super.putPayload("company", company);
    }

    public StoreEntity getStore() {
        return super.getNullAbleValue("store", StoreEntity.class);
    }

    public CompanyEntity getCompany() {
        return super.getNullAbleValue("company", CompanyEntity.class);
    }

    void setEmployee(EmployeeEntity employee) {
        super.putPayload("employee", employee);
    }

    public EmployeeEntity getEmployee() {
        return super.getNullAbleValue("employee", EmployeeEntity.class);
    }

}
