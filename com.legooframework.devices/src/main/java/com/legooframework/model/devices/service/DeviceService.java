package com.legooframework.model.devices.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;

public abstract class DeviceService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("devicesBundle", Bundle.class);
    }

}
