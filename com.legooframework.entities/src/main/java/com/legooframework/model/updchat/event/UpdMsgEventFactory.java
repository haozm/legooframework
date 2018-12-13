package com.legooframework.model.updchat.event;

import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.updchat.entity.UDPMessage;
import org.apache.commons.lang3.StringUtils;

public final class UpdMsgEventFactory {

    private static final String EVENT_UPD_RCEIVE_MSG_EVENT = "updRceiveMsgEvent";
    private static final String EVENT_UPD_RCEIVE_ACKMSG_EVENT = "updRceiveAckMsgEvent";
    private static final String EVENT_UPDMONITORHISMSGEVENT_EVENT = "updMonitorHisMsgEvent";
    private static final String EVENT_UPD_LOGIN_EVENT = "updLoginEvent";
    private static final String EVENT_UPD_LOGIN_REQ_EVENT = "updLoginReqEvent";
    private static final String EVENT_UPD_LOGOUT_REQ_EVENT = "updLogoutReqEvent";
    private static final String EVENT_UPDMONITORPREMSGEVENT_EVENT = "updMonitorPreMsgEvent";

    public static boolean isUDPLogoutReqEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_UPD_LOGOUT_REQ_EVENT, event.getEventName());
    }

    public static LegooEvent updLogoutReqEvent() {
        return new UpdMsgModuleEvent(EVENT_UPD_LOGOUT_REQ_EVENT);
    }

    public static boolean isUDPLoginReqEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_UPD_LOGIN_REQ_EVENT, event.getEventName());
    }

    public static LegooEvent updLoginReqEvent() {
        return new UpdMsgModuleEvent(EVENT_UPD_LOGIN_REQ_EVENT);
    }

    public static boolean isUpdMonitorHisMsgEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_UPDMONITORHISMSGEVENT_EVENT, event.getEventName());
    }

    public static boolean isUdpMonitorPreMsgEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_UPDMONITORPREMSGEVENT_EVENT, event.getEventName());
    }

    public static UpdMsgModuleEvent updMonitorHisMsgEvent(UDPMessage message) {
        UpdMsgModuleEvent moduleEvent = new UpdMsgModuleEvent(EVENT_UPDMONITORHISMSGEVENT_EVENT);
        moduleEvent.setUDPChatMessage(message);
        return moduleEvent;
    }

    public static UpdMsgModuleEvent udpMonitorPreMsgEvent(UDPMessage message) {
        UpdMsgModuleEvent moduleEvent = new UpdMsgModuleEvent(EVENT_UPDMONITORPREMSGEVENT_EVENT);
        moduleEvent.setUDPChatMessage(message);
        return moduleEvent;
    }

    public static boolean isUpdRceiveMsgEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_UPD_RCEIVE_MSG_EVENT, event.getEventName());
    }

    public static UpdMsgModuleEvent updRceiveMsgEvent(UDPMessage chatMessage) {
        UpdMsgModuleEvent moduleEvent = new UpdMsgModuleEvent(EVENT_UPD_RCEIVE_MSG_EVENT);
        moduleEvent.setUDPChatMessage(chatMessage);
        return moduleEvent;
    }

    public static UpdMsgModuleEvent updRceiveAckMsgEvent(UDPMessage chatMessage) {
        UpdMsgModuleEvent moduleEvent = new UpdMsgModuleEvent(EVENT_UPD_RCEIVE_ACKMSG_EVENT);
        moduleEvent.setUDPChatMessage(chatMessage);
        return moduleEvent;
    }

    public static boolean isUpdLoginEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_UPD_LOGIN_EVENT, event.getEventName());
    }

    public static UpdMsgModuleEvent updLoginEvent() {
        return new UpdMsgModuleEvent(EVENT_UPD_LOGIN_EVENT);
    }

}
