package com.legooframework.model.scheduler.service;

import com.legooframework.model.scheduler.entity.JobRunTrajectoryListener;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class SchedulerJobsFactoryBean extends AbstractFactoryBean<Scheduler> {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerJobsFactoryBean.class);

    private StdSchedulerFactory sf = new StdSchedulerFactory();
    private Scheduler scheduler;

    @Override
    public Class<Scheduler> getObjectType() {
        return Scheduler.class;
    }

    @Override
    protected Scheduler createInstance() throws Exception {
//        schedulerFactory.
        scheduler = sf.getScheduler();
        if (jobRunTrajectoryListener != null)
            scheduler.getListenerManager().addJobListener(jobRunTrajectoryListener);
        scheduler.startDelayed(5);
        return scheduler;
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


    private JobRunTrajectoryListener jobRunTrajectoryListener;

    public void setJobRunTrajectoryListener(JobRunTrajectoryListener jobRunTrajectoryListener) {
        this.jobRunTrajectoryListener = jobRunTrajectoryListener;
    }
}
