package com.legooframework.model.usergroups.event;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import org.apache.commons.lang3.StringUtils;

public class GroupEventFactory {

    private static final String EVENT_LOADWECHATGROUPSBYSTOREIDEVENT = "loadWechatGroupsByStroreIdEvent";

    public static LegooEvent loadWechatGroupsByStoreIdEvent(Bundle bundle, Long storeId) {
        Preconditions.checkNotNull(storeId, "入参 storeId 不可以为空.");
        Preconditions.checkNotNull(bundle, "入参 bundle 不可以为空.");
        GroupModuleEvent event = new GroupModuleEvent(bundle.getName(), EVENT_LOADWECHATGROUPSBYSTOREIDEVENT);
        event.setStoreId(storeId);
        return event;
    }

    public static boolean isLoadWechatGroupsEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADWECHATGROUPSBYSTOREIDEVENT, event.getEventName());
    }


}
