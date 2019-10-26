package com.legooframework.model.amqp.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.messaging.Message;

import java.util.Collection;

public class CollectMessageSplitter extends AbstractMessageSplitter {

    private static final Logger logger = LoggerFactory.getLogger(CollectMessageSplitter.class);

    @Override
    protected Object splitMessage(Message<?> message) {
        Object payload = message.getPayload();
        if (payload instanceof Collection) {
            return payload;
        }
        return null;
    }

    public void log(Message<?> payload) {
        logger.debug(payload.toString());
    }
}
