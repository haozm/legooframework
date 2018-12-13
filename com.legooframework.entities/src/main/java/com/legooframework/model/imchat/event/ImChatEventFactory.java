package com.legooframework.model.imchat.event;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.imchat.entity.ImMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;

public final class ImChatEventFactory {

    private static final String EVENT_WECHAT_ACK_MSG_EVENT = "wechatAckMsgEvent";
    private static final String EVENT_WECHAT_REQ_MSG_EVENT = "wechatReqMsgEvent";

    private static final String EVENT_CREATE_CHATONLINESESSION = "createChatOnlineSessionEvent";
    private static final String EVENT_DESTORY_CHATONLINESESSION = "destoryChatOnlineSessionEvent";

    private static final String EVENT_LOAD_MEMORYCONTACT_EVENT = "loadMemoryContactEvent";

    private static final String EVENT_SENDIMMESSAGE_EVENT = "sendImMessageEvent";

    // 发送websocket事件
    public static boolean isSendImMessageEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_SENDIMMESSAGE_EVENT, event.getEventName());
    }

    public static LegooEvent sendImMessageEvent(Bundle bundle, ImMessage message) {
        ImChatModuleEvent event = new ImChatModuleEvent(bundle, EVENT_SENDIMMESSAGE_EVENT);
        event.setSendImMessage(message);
        return event;
    }

    public static boolean isLoadMemoryContactEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOAD_MEMORYCONTACT_EVENT, event.getEventName());
    }

    public static LegooEvent loadMemoryContactEvent() {
        ImChatModuleEvent event = new ImChatModuleEvent(EVENT_LOAD_MEMORYCONTACT_EVENT);
        return event;
    }

    public static ImChatModuleEvent createChatOnlineSessionEvent(LoginContext user) {
        ImChatModuleEvent event = new ImChatModuleEvent(EVENT_CREATE_CHATONLINESESSION);
        event.setUserDetails(user);
        return event;
    }

    public static ImChatModuleEvent destoryChatOnlineSessionEvent(LoginContext user, IoSession session) {
        ImChatModuleEvent event = new ImChatModuleEvent(EVENT_DESTORY_CHATONLINESESSION);
        event.setUserDetails(user);
        event.setIoSession(session);
        return event;
    }

    public static boolean isDestoryChatOnlineSessionEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_DESTORY_CHATONLINESESSION, event.getEventName());
    }

    public static boolean isCreateChatOnlineSessionEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_CREATE_CHATONLINESESSION, event.getEventName());
    }

    public static boolean isWechatAckMsgEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_WECHAT_ACK_MSG_EVENT, event.getEventName());
    }

    public static boolean isWechatReqMsgEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_WECHAT_REQ_MSG_EVENT, event.getEventName());
    }

    public static ImChatModuleEvent wechatReqMsgEvent(ImMessage reqMessage) {
        ImChatModuleEvent moduleEvent = new ImChatModuleEvent(EVENT_WECHAT_REQ_MSG_EVENT);
        moduleEvent.setChatReqMessage(reqMessage);
        return moduleEvent;
    }

    public static ImChatModuleEvent wechatAckMsgEvent(ImMessage ackMessage) {
        ImChatModuleEvent moduleEvent = new ImChatModuleEvent(EVENT_WECHAT_ACK_MSG_EVENT);
        moduleEvent.setChatAckMessage(ackMessage);
        return moduleEvent;
    }

}
