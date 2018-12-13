package com.legooframework.model.regiscenter.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.event.LegooEvent;
import org.apache.commons.lang3.StringUtils;

public final class RegisCenterEventFactory {

    private static final String EVENT_ACTIVEDEVICEBYPINCODE = "activeDeviceByPinCodeEvent";

    public static boolean isActiveDeviceByPinCodeEvent(LegooEvent event) {
        return StringUtils.equals(EVENT_ACTIVEDEVICEBYPINCODE, event.getEventName());
    }

    public static LegooEvent activeDeviceByPinCodeEvent(String deviceId, String imei1, String imei2, Long companyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "设备Id deviceId 不可以为空.");
        RegisCenterEvent event = new RegisCenterEvent(EVENT_ACTIVEDEVICEBYPINCODE);
        event.setDeviceId(deviceId);
        event.setCompanyId(companyId);
        event.setImei1(imei1);
        event.setImei2(imei2);
        return event;
    }

}
