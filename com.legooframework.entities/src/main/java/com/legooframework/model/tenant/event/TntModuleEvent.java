package com.legooframework.model.tenant.event;

import com.legooframework.model.core.event.LegooEvent;

public class TntModuleEvent extends LegooEvent {

    public TntModuleEvent(String source, String target, String eventName) {
        super(source, target, eventName);
    }

    public TntModuleEvent(String source, String eventName) {
        super(source, eventName);
    }
}
