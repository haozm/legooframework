package com.legooframework.model.scheduler.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

public class JobRunTrajectoryEntity extends BaseEntity<Long> {

    private String jobName, message;
    private Date fireTime, finshTime;
    private long jobRunTime;
    private boolean error;

    JobRunTrajectoryEntity(JobExecutionContext context, JobExecutionException jobException) {
        super(0L);
        this.jobName = context.getJobDetail().getKey().toString();
        this.jobRunTime = context.getJobRunTime();
        this.fireTime = context.getFireTime();
        this.error = jobException != null;
        this.message = jobException == null ? "success" : jobException.getMessage();
        this.finshTime = context.getTrigger().getEndTime();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jobName", jobName)
                .add("message", message)
                .add("fireTime", fireTime)
                .add("finshTime", finshTime)
                .add("jobRunTime", jobRunTime)
                .add("error", error)
                .toString();
    }
}
