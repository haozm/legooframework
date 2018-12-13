package com.legooframework.model.updchat.event;

import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.updchat.entity.UDPMessage;

public class UpdMsgModuleEvent extends LegooEvent {

    UpdMsgModuleEvent(String eventName) {
        super("imchat", eventName);
    }

    void setUDPChatMessage(UDPMessage chatmsg) {
        putPayload("UDPMessage", chatmsg);
    }

    public UDPMessage getUDPMessage() {
        return super.getNullAbleValue("UDPMessage", UDPMessage.class);
    }

}
