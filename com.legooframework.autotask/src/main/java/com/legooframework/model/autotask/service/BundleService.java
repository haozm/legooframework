package com.legooframework.model.autotask.service;

import com.legooframework.model.autotask.entity.TaskExecuteEntityAction;
import com.legooframework.model.autotask.entity.TaskExecuteJobDetailBuilder;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import org.quartz.Scheduler;
import org.springframework.batch.core.launch.JobLauncher;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("autotaskBundle", Bundle.class);
    }

    JobLauncher getJobLauncher() {
        return getBean("jobLauncher", JobLauncher.class);
    }

    TaskExecuteEntityAction getTaskExecuteAction() {
        return getBean("taskExecuteEntityAction", TaskExecuteEntityAction.class);
    }

    Scheduler getScheduler() {
        return getBean("autotaskJobsScheduler", Scheduler.class);
    }

    TaskExecuteJobDetailBuilder getTaskExecuteJobDetail() {
        return getBean("autoTaskExecuteJobDetail", TaskExecuteJobDetailBuilder.class);
    }

    public static String CHANNEL_TASK_RUNING = "task-running-channel";
}
