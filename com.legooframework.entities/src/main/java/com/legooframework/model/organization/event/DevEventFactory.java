package com.legooframework.model.organization.event;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.organization.entity.EquipmentEntity;
import org.apache.commons.lang3.StringUtils;

public final class DevEventFactory {

    private static final String EVENT_ENABLED_DEVICED = "enabledDeviceEvent";
    private static final String EVENT_DISABELD_DEVICED = "disabeldDeviceEvent";

    public static boolean isEnabledDeviceEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_ENABLED_DEVICED, event.getEventName());
    }

    public static LegooEvent enabledDeviceEvent(EquipmentEntity equipment) {
        Preconditions.checkNotNull(equipment, "入参 equipment 不可以为空.");
        DevModuleEvent event = new DevModuleEvent(EVENT_ENABLED_DEVICED);
        event.setEquipmentEntity(equipment);
        return event;
    }

    public static boolean isDisabeldDeviceEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_DISABELD_DEVICED, event.getEventName());
    }

    public static LegooEvent disabeldDeviceEvent(EquipmentEntity equipment) {
        Preconditions.checkNotNull(equipment, "入参 equipment 不可以为空.");
        DevModuleEvent event = new DevModuleEvent(EVENT_DISABELD_DEVICED);
        event.setEquipmentEntity(equipment);
        return event;
    }
}
