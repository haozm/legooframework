package com.legooframework.model.scheduler.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import org.quartz.Scheduler;

public class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("schedulerBundle", Bundle.class);
    }
}
