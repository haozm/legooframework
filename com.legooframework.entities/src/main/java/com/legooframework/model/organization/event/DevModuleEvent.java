package com.legooframework.model.organization.event;

import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.organization.entity.EquipmentEntity;

public class DevModuleEvent extends LegooEvent {

    DevModuleEvent(String source, String eventName) {
        super(source, "organization", eventName);
    }

    DevModuleEvent(String eventName) {
        super("organization", eventName);
    }

    void setEquipmentEntity(EquipmentEntity equipment) {
        this.payload.put("EquipmentEntity", equipment);
    }

    public EquipmentEntity getEquipmentEntity() {
        return super.getNullAbleValue("EquipmentEntity", EquipmentEntity.class);
    }
}
