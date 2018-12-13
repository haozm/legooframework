package com.legooframework.model.tenant.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.osgi.Bundle;
import org.apache.commons.lang3.StringUtils;

public final class TntEventFactory {

    private static final String EVENT_FINDACCOUNTBYNOEVENT = "findAccountByNoEvent";

    public static TntEventFactory findAccountByNoEvent(Bundle source, String accountNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "入参 accountNo 不可以为空.");
        return null;
    }

    public static boolean isFindAccountByNoEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_FINDACCOUNTBYNOEVENT, event.getEventName());
    }
}
