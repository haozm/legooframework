package com.legooframework.model.organization.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.organization.entity.EquipmentEntity;
import com.legooframework.model.organization.entity.EquipmentEntityAction;
import com.legooframework.model.organization.entity.StoreEntityAction;
import com.legooframework.model.organization.event.DevEventFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class EquipmentService extends OrgService {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentService.class);

    public void enbaledDevice(String deviceId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(deviceId), "入参 String deviceId 不可以为空值...");
        int res = getBean(EquipmentEntityAction.class).enableDevice(deviceId);
        if (res == 1) {
            Optional<EquipmentEntity> exits = getBean(EquipmentEntityAction.class).findById(deviceId);
            Preconditions.checkState(exits.isPresent(), "获取enbaledDevice(%s)的设备失败...", deviceId);
            getEventBus().postEvent(DevEventFactory.enabledDeviceEvent(exits.get()));
        }
    }

    public void disabledDevice(String deviceId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(deviceId), "入参 String deviceId 不可以为空值...");
        int res = getBean(EquipmentEntityAction.class).disabeldDevice(deviceId);
        if (res == 0) return;
        Optional<EquipmentEntity> equipment = getBean(EquipmentEntityAction.class).findById(deviceId);
        Preconditions.checkState(equipment.isPresent(), "获取enbaledDevice(%s)的设备失败...", deviceId);
        getBean(StoreEntityAction.class).unBindingByDevice(equipment.get());
    }



}
