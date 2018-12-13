package com.legooframework.model.imchat.event;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.imchat.entity.ImMessage;
import org.apache.mina.core.session.IoSession;

public class ImChatModuleEvent extends LegooEvent {

    ImChatModuleEvent(String eventName) {
        super("imchat", eventName);
    }

    ImChatModuleEvent(Bundle bundle, String eventName) {
        super(bundle.getName(), "imchat", eventName);
    }

    void setChatReqMessage(ImMessage reqMessage) {
        putPayload("ImReqMessage", reqMessage);
    }

    public ImMessage getChatReqMessage() {
        return super.getNullAbleValue("ImReqMessage", ImMessage.class);
    }

    void setChatAckMessage(ImMessage ackMessage) {
        putPayload("ImAckMessage", ackMessage);
    }

    public ImMessage getChatAckMessage() {
        return super.getNullAbleValue("ImAckMessage", ImMessage.class);
    }

    void setUserDetails(LoginContext userDetail) {
        putPayload("UserDetails", userDetail);
    }

    public LoginContext getUserDetails() {
        return super.getNullAbleValue("UserDetails", LoginContext.class);
    }

    void setSendImMessage(ImMessage message) {
        putPayload("sendImMessage", message);
    }

    public ImMessage getSendImMessage() {
        return super.getNullAbleValue("sendImMessage", ImMessage.class);
    }

    void setIoSession(IoSession session) {
        putPayload("IoSession", session);
    }

    public IoSession getIoSession() {
        return super.getNullAbleValue("IoSession", IoSession.class);
    }

}
