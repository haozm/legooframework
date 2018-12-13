package com.legooframework.model.webwork.service;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.event.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Optional;

public class EventListenerService extends WebBaseService {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class);

    public Message<?> handleMessage(@Header(name = "loginContext") LoginContext loginContext,
                                    @Payload LegooEvent event) {
        LoginContextHolder.setCtx(loginContext);
        if (logger.isTraceEnabled())
            logger.trace(String.format("handleMessage(%s)", event));
        return MessageHelper.buildResponse(event, Optional.empty());
    }

}
