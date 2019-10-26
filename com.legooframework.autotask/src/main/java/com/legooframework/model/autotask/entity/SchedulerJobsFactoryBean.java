package com.legooframework.model.autotask.entity;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class SchedulerJobsFactoryBean extends AbstractFactoryBean<Scheduler> {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerJobsFactoryBean.class);

    private Scheduler scheduler;
    private TaskExecuteJobListener jobListener;

    public void setJobListener(TaskExecuteJobListener jobListener) {
        this.jobListener = jobListener;
    }

    @Override
    public Class<Scheduler> getObjectType() {
        return Scheduler.class;
    }

    @Override
    protected Scheduler createInstance() throws Exception {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        if (jobListener != null)
            this.scheduler.getListenerManager().addJobListener(jobListener);
        this.scheduler.start();
        if (logger.isInfoEnabled())
            logger.info("StdSchedulerFactory.getDefaultScheduler() is ok...");
        return this.scheduler;
    }

    public void shutdown() {
        if (scheduler == null) return;
        try {
            scheduler.shutdown(true);
            if (logger.isInfoEnabled())
                logger.info("scheduler.shutdown(true) is finshed...");
        } catch (Exception e) {
            logger.error("scheduler.shutdown(true) scheduler  has error", e);
        }
    }

}
