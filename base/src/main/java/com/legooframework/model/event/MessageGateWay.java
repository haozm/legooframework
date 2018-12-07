package com.legooframework.model.event;

import java.util.Optional;

public interface MessageGateWay {

    void postEvent(LegooEvent event);

    <T> Optional<T> sendAndReceive(LegooEvent event, Class<T> clazz) throws Exception;

    // 直接发送到指定队列
    void send(LegooEvent event) throws Exception;

}
