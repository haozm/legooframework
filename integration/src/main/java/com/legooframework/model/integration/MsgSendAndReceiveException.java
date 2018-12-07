package com.legooframework.model.integration;

import com.legooframework.model.base.exception.BaseException;
import com.legooframework.model.event.LegooEvent;
import org.springframework.messaging.Message;

public class MsgSendAndReceiveException extends BaseException {

    public MsgSendAndReceiveException(LegooEvent event, Throwable cause) {
        super("7000", String.format("SendAndReceive(%s) has error.", event.getEventName()), cause);
    }
}
