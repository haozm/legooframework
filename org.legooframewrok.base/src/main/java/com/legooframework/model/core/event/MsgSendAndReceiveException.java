package com.legooframework.model.core.event;

import com.legooframework.model.core.base.exception.BaseException;

public class MsgSendAndReceiveException extends BaseException {

    public MsgSendAndReceiveException(LegooEvent event, Throwable cause) {
        super("7000", String.format("SendAndReceive(%s) has error.", event.getEventName()), cause);
    }
}
