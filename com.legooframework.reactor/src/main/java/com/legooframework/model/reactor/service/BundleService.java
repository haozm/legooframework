package com.legooframework.model.reactor.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("reactorBundle", Bundle.class);
    }
    
    JobLauncher getJobLauncher() {
        return getBean("jobLauncher", JobLauncher.class);
    }
}
