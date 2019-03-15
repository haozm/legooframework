package com.csosm.commons.server;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadEventSub {

    private static final Logger logger = LoggerFactory.getLogger(DeadEventSub.class);

    DeadEventSub() {
    }

    @Subscribe
    public void nothing(DeadEvent event) {
        logger.warn(String.format("%s=%s from dead event", event.getSource().getClass(), event.getEvent()));
    }
}
