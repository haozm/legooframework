package com.legooframework.model.event;

import com.google.common.base.Preconditions;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.Optional;

public class MessageHelper {

    public static <T> Message<Optional<T>> buildResponse(LegooEvent event, Optional<T> paylaod) {
        return MessageBuilder.withPayload(paylaod)
                .setHeader("event", event)
                .setHeader("hasError", false)
                .build();
    }

    public static boolean hasException(Message<?> message) {
        Boolean res = message.getHeaders().get("hasError", Boolean.class);
        return res == null ? false : res;
    }

    public static Message<Exception> buildException(LegooEvent event, Exception e) {
        Preconditions.checkNotNull(e);
        return MessageBuilder.withPayload(e)
                .setHeader("event", event)
                .setHeader("hasError", true)
                .build();
    }
}
