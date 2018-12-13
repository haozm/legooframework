package com.legooframework.model.organization.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.event.MessageHelper;
import com.legooframework.model.devices.entity.DeviceEntity;
import com.legooframework.model.devices.entity.DeviceEntityAction;
import com.legooframework.model.organization.entity.*;
import com.legooframework.model.organization.event.OrgEventFactory;
import com.legooframework.model.organization.event.OrgModuleEvent;
import com.legooframework.model.regiscenter.event.RegisCenterEvent;
import com.legooframework.model.regiscenter.event.RegisCenterEventFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Optional;

/**
 * 模块事件监听
 */
public class EventListenerService extends OrgService {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class);

    public Message<?> handleMessage(@Header(name = "loginContext") LoginContext loginContext,
                                    @Payload LegooEvent event) {
        LoginContextHolder.setCtx(loginContext);
        if (logger.isDebugEnabled())
            logger.debug(event.toString());
        try {
            if (OrgEventFactory.isCompanyAddStoreEvent(event)) {
                OrgModuleEvent moduleEvent = (OrgModuleEvent) event;
                getBean(StoreEntityAction.class).evictEntity(moduleEvent.getStore());
            } else if (OrgEventFactory.isLoadStoreByIdEvent(event)) {
                OrgModuleEvent moduleEvent = (OrgModuleEvent) event;
                Optional<StoreEntity> store = getBean(StoreEntityAction.class)
                        .findById(moduleEvent.getStoreId());
                store.ifPresent(x -> Preconditions.checkState(x.isEffective(), "请求门店%s状态%s",
                        x.getFullName(), x.getStoreStatusName()));
                return MessageHelper.buildResponse(event, store);
            } else if (OrgEventFactory.isLoadEmployeeAggEvent(event)) {
                OrgModuleEvent moduleEvent = (OrgModuleEvent) event;
                return MessageHelper.buildResponse(event, getBean(EmployeeService.class)
                        .loadEmployeeAggByAccount(moduleEvent.getAccountEntity()));
            } else if (OrgEventFactory.isFindEquipmentByIdEvent(event)) {
                OrgModuleEvent moduleEvent = (OrgModuleEvent) event;
                return MessageHelper.buildResponse(event, getBean(EquipmentEntityAction.class)
                        .findById(moduleEvent.getEquipmentId()));
            } else if (OrgEventFactory.isBindingVDeviceToStoreEvent(event)) {
                OrgModuleEvent moduleEvent = (OrgModuleEvent) event;
                getBean(StoreService.class).bindingDeviceToStore(moduleEvent.getStoreId(), moduleEvent.getDeviceId());
                return MessageHelper.buildResponse(event, Optional.of("OK"));
            } else if (OrgEventFactory.isLoadcompanyByIdEvent(event)) {
                OrgModuleEvent moduleEvent = (OrgModuleEvent) event;
                Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(moduleEvent.getCompanyId());
                return MessageHelper.buildResponse(event, company);
            } else if (OrgEventFactory.isCheckStoreIdsByCompany(event)) {
                OrgModuleEvent moduleEvent = (OrgModuleEvent) event;
                List<Long> storeIds = moduleEvent.getStoreIds();
                if (CollectionUtils.isEmpty(storeIds)) return MessageHelper.buildResponse(event, Optional.empty());
                Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(moduleEvent.getCompanyId());
                Preconditions.checkState(company.isPresent(), "Id=%s 对应的公司不存在...",
                        moduleEvent.getCompanyId());
                Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class).loadStoresByCompany(company.get(),
                        null);
                return MessageHelper.buildResponse(event, stores);
            } else if (RegisCenterEventFactory.isActiveDeviceByPinCodeEvent(event)) {
                RegisCenterEvent regisCenterEvent = (RegisCenterEvent) event;
                DeviceEntity device = getBean(DeviceEntityAction.class).saveOrUpdate(regisCenterEvent.getDeviceId(),
                        "大V手机", null, null, null, null, 0, null, null, 0d, DeviceEntity.OsType.ANDROID, null, null, null, null,
                        regisCenterEvent.getImei1(), regisCenterEvent.getImei2());
                Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(regisCenterEvent.getCompanyId());
                Preconditions.checkState(company.isPresent());
                getBean(EquipmentEntityAction.class).insertXDevice(device, "激活自动注册", company.get());
                return MessageHelper.buildResponse(event, company);
            }
        } catch (Exception e) {
            logger.error(String.format("hold event=%s has error...", event), e);
            return MessageHelper.buildException(event, e);
        }
        return MessageHelper.buildResponse(event, Optional.empty());
    }

}
