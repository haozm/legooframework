package com.legooframework.model.reactor.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.covariant.entity.TemplateEntityAction;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.integration.core.MessagingTemplate;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("reactorBundle", Bundle.class);
    }

    JobLauncher getJobLauncher() {
        return getBean("jobLauncher", JobLauncher.class);
    }

    TemplateEntityAction templateAction;
    MessagingTemplate messagingTemplate;

    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void setTemplateAction(TemplateEntityAction templateAction) {
        this.templateAction = templateAction;
    }

}
