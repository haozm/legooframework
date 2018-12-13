package com.legooframework.model.dict.event;

import com.legooframework.model.core.event.LegooEvent;

public class DictModuleEvent extends LegooEvent {

    DictModuleEvent(String source, String eventName) {
        super(source, "dict", eventName);
    }

    void setDictType(String dictType) {
        super.putPayload("dictType", dictType);
    }

    public String getDictType() {
        return super.getString("dictType");
    }

}
