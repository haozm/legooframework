package com.legooframework.model.organization.event;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.organization.entity.CompanyEntity;
import com.legooframework.model.organization.entity.EmployeeEntity;
import com.legooframework.model.organization.entity.StoreEntity;
import com.legooframework.model.security.entity.AccountEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public final class OrgEventFactory {

    private static final String EVENT_ADDSTOREEVENT = "addStoreEvent";
    private static final String EVENT_COMPNAY_ADDSTORE = "companyAddedStoreEvent";
    private static final String EVENT_LOADEMPLOYEEAGGEVENT = "loadEmployeeAggEvent";
    private static final String EVENT_LOADSTOREBYIDEVENT = "loadStoreByIdEvent";

    private static final String EVENT_LOADCOMPANYBYID_EVENT = "loadcompanyByIdEvent";
    private static final String EVENT_FINDEQUIPMENTBYIDEVENT = "findEquipmentByIdEvent";
    private static final String EVENT_BINDINGVDEVICETOSTOREEVENT = "bindingVDeviceToStoreEvent";

    private static final String EVENT_CHECKSTOREIDSBYCOMPANY = "checkStoreIdsByCompany";

    private static final String EVENT_NOTICEQUITEMPLOYEEEVENT = "noticeQuitEmployeeEvent";
    private static final String EVENT_NOTICEADDEDEMPLOYEEEVENT = "noticeAddedEmpoyeeEvent";

    public static boolean isCheckStoreIdsByCompany(LegooEvent event) {
        return StringUtils.equals(EVENT_CHECKSTOREIDSBYCOMPANY, event.getEventName());
    }

    public static LegooEvent checkStoreIdsByCompany(Bundle bundle, Long companyId, List<Long> storeIds) {
        Preconditions.checkNotNull(companyId, "入参 companyId 不可以为空.");
        Preconditions.checkNotNull(bundle, "入参 bundle 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(bundle.getName(), EVENT_CHECKSTOREIDSBYCOMPANY);
        event.setCompanyId(companyId);
        event.setStoreIds(storeIds);
        return event;
    }

    public static boolean isLoadcompanyByIdEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADCOMPANYBYID_EVENT, event.getEventName());
    }

    public static LegooEvent loadcompanyByIdEvent(Bundle bundle, Long companyId) {
        Preconditions.checkNotNull(companyId, "入参 companyId 不可以为空.");
        Preconditions.checkNotNull(bundle, "入参 bundle 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(bundle.getName(), EVENT_LOADCOMPANYBYID_EVENT);
        event.setCompanyId(companyId);
        return event;
    }

    public static boolean isBindingVDeviceToStoreEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_BINDINGVDEVICETOSTOREEVENT, event.getEventName());
    }

    public static LegooEvent bindingVDeviceToStoreEvent(Bundle bundle, Long storeId, String deviceId) {
        Preconditions.checkNotNull(deviceId, "入参 deviceId 不可以为空.");
        Preconditions.checkNotNull(storeId, "入参 storeId 不可以为空.");
        Preconditions.checkNotNull(bundle, "入参 bundle 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(bundle.getName(), EVENT_BINDINGVDEVICETOSTOREEVENT);
        event.setStoreId(storeId);
        event.setDeviceId(deviceId);
        return event;
    }

    public static boolean isFindEquipmentByIdEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_FINDEQUIPMENTBYIDEVENT, event.getEventName());
    }

    public static LegooEvent findEquipmentByIdEvent(Bundle bundle, String imie) {
        Preconditions.checkNotNull(imie, "入参 imie 不可以为空.");
        Preconditions.checkNotNull(bundle, "入参 bundle 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(bundle.getName(), EVENT_FINDEQUIPMENTBYIDEVENT);
        event.setEquipmentId(imie);
        return event;
    }

    public static boolean isLoadStoreByIdEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADSTOREBYIDEVENT, event.getEventName());
    }

    public static LegooEvent loadStoreByIdEvent(Bundle bundle, Long storeId) {
        Preconditions.checkNotNull(storeId, "入参 storeId 不可以为空.");
        Preconditions.checkNotNull(bundle, "入参 bundle 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(bundle.getName(), EVENT_LOADSTOREBYIDEVENT);
        event.setStoreId(storeId);
        return event;
    }

    public static boolean isLoadEmployeeAggEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADEMPLOYEEAGGEVENT, event.getEventName());
    }

    public static LegooEvent loadEmployeeAggEvent(Bundle bundle, AccountEntity account) {
        Preconditions.checkNotNull(account, "入参 account 不可以为空.");
        Preconditions.checkNotNull(bundle, "入参 bundle 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(bundle.getName(), EVENT_LOADEMPLOYEEAGGEVENT);
        event.setAccountEntity(account);
        return event;
    }

    public static OrgModuleEvent addStoreEvent(StoreEntity store) {
        Preconditions.checkNotNull(store, "入参 store 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(EVENT_ADDSTOREEVENT);
        event.setStore(store);
        return event;
    }

    public static boolean isAddStoreEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_ADDSTOREEVENT, event.getEventName());
    }

    public static OrgModuleEvent companyAddStoreEvent(StoreEntity store, CompanyEntity company) {
        Preconditions.checkNotNull(store, "入参 store 不可以为空.");
        Preconditions.checkNotNull(company, "入参 company 不可以为空.");
        OrgModuleEvent event = new OrgModuleEvent(EVENT_COMPNAY_ADDSTORE);
        event.setStore(store);
        event.setCompany(company);
        return event;
    }

    public static boolean isCompanyAddStoreEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_COMPNAY_ADDSTORE, event.getEventName());
    }

    public static boolean isNoticeQuitEmployeeEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_NOTICEQUITEMPLOYEEEVENT, event.getEventName());
    }

    public static OrgModuleEvent noticeQuitEmployeeEvent(EmployeeEntity employee) {
        Objects.requireNonNull(employee);
        OrgModuleEvent event = new OrgModuleEvent(EVENT_NOTICEQUITEMPLOYEEEVENT);
        event.setEmployee(employee);
        return event;
    }

    public static boolean isNoticeAddedEmployeeEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_NOTICEADDEDEMPLOYEEEVENT, event.getEventName());
    }

    public static OrgModuleEvent noticeAddedEmployeeEvent(EmployeeEntity employee) {
        Objects.requireNonNull(employee);
        OrgModuleEvent event = new OrgModuleEvent(EVENT_NOTICEADDEDEMPLOYEEEVENT);
        event.setEmployee(employee);
        return event;
    }
}
