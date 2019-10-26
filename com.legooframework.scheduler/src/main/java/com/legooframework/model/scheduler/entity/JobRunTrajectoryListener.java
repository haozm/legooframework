package com.legooframework.model.scheduler.entity;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunTrajectoryListener extends JobListenerSupport {

    private static final Logger logger = LoggerFactory.getLogger(JobRunTrajectoryListener.class);

    @Override
    public String getName() {
        return JobRunTrajectoryListener.class.getName();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobRunTrajectoryEntity trajectory = new JobRunTrajectoryEntity(context, jobException);
        logger.debug(trajectory.toString());
    }
}
