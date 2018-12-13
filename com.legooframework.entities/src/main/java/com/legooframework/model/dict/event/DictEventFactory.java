package com.legooframework.model.dict.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import org.apache.commons.lang3.StringUtils;

public final class DictEventFactory {

    private static final String EVENT_LOADDICTBYTYPEEVENT = "loadDictByTypeEvent";

    public static DictModuleEvent loadDictByTypeEvent(Bundle source, String type) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "入参 type 不可以为空.");
        DictModuleEvent event = new DictModuleEvent(source.getName(), EVENT_LOADDICTBYTYPEEVENT);
        event.setDictType(type);
        return event;
    }

    public static boolean isLoadDictByTypeEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_LOADDICTBYTYPEEVENT, event.getEventName());
    }
}
