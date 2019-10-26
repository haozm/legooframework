package com.csosm.module.base;

import com.csosm.commons.event.EventBusSubscribe;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.SystemlogEntity;
import com.csosm.module.base.entity.SystemlogEntityAction;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBusSubscribeService extends AbstractBaseServer implements EventBusSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(EventBusSubscribeService.class);

    @Subscribe
    public void subSysLogEvent(SystemlogEntity event) {
        if (logger.isDebugEnabled()) logger.debug(String.format("Subscribe Event %s", event));
        getBean(SystemlogEntityAction.class).insert(event);
    }
}
