package com.legooframework.model.autotask.entity;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskExecuteJobListener implements JobListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteJobListener.class);

    @Override
    public String getName() {
        return TaskExecuteJobListener.class.toString();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("jobExecutionVetoed(JobExecutionContext=%s)", context));
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("jobWasExecuted(JobExecutionContext=%s,JobExecutionException=%s)", context,
                    jobException));
        try {
            context.getScheduler().unscheduleJob(context.getTrigger().getKey());
            if (logger.isDebugEnabled())
                logger.debug(String.format("unscheduleJob(%s) is finished...", context.getTrigger().getKey()));
        } catch (Exception e) {
            logger.error(String.format("unscheduleJob(%s) has exception", context.getTrigger().getKey()), e);
        }

    }
}
