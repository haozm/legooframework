package com.legooframework.model.usergroups.event;

import com.legooframework.model.core.event.LegooEvent;
import org.apache.commons.collections4.MapUtils;

public class GroupModuleEvent extends LegooEvent {

    GroupModuleEvent(String source, String eventName) {
        super(source, "usergroups", eventName);
    }

    GroupModuleEvent(String eventName) {
        super("usergroups", eventName);
    }

    void setStoreId(Long storeId) {
        super.putPayload("storeId", storeId);
    }

    public Long getStoreId() {
        return MapUtils.getLong(super.payload, "storeId");
    }

}
