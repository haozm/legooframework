package com.csosm.module.webchat;

import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.webchat.entity.ChatRoomContactEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatGroupServer extends AbstractBaseServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatGroupServer.class);

    // 统计活跃度
    public void totalGroupActivity() {
        getBean(ChatRoomContactEntityAction.class).totalGroupActivity();
    }

}
