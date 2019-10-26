package com.legooframework.model.autotask.entity;

import org.quartz.*;
import org.quartz.utils.ClassUtils;
import org.springframework.integration.core.MessagingTemplate;

public class TaskExecuteJobDetailBuilder implements JobDetail {

    private JobKey jobKey;
    private JobDataMap jobDataMap;
    private TaskExecuteEntity taskExecute;
    private MessagingTemplate messagingTemplate;

    public TaskExecuteJobDetailBuilder() {
    }

    @Override
    public JobKey getKey() {
        if (jobKey == null) this.jobKey = new JobKey(String.format("JobKey:%s", taskExecute.getUuid()));
        return this.jobKey;
    }

    @Override
    public String getDescription() {
        return taskExecute.toString();
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return TaskExecuteJob.class;
    }

    @Override
    public JobDataMap getJobDataMap() {
        if (this.jobDataMap == null) {
            this.jobDataMap = new JobDataMap();
            this.jobDataMap.put(MessagingTemplate.class.getSimpleName(), messagingTemplate);
            this.jobDataMap.put(TaskExecuteEntity.class.getSimpleName(), taskExecute);
        }
        return this.jobDataMap;
    }

    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void setTaskExecute(TaskExecuteEntity taskExecute) {
        this.taskExecute = taskExecute;
    }


    @Override
    public boolean isDurable() {
        return false;
    }

    @Override
    public boolean isPersistJobDataAfterExecution() {
        return ClassUtils.isAnnotationPresent(this.getClass(), PersistJobDataAfterExecution.class);
    }

    @Override
    public boolean isConcurrentExectionDisallowed() {
        return ClassUtils.isAnnotationPresent(this.getClass(), DisallowConcurrentExecution.class);
    }

    @Override
    public boolean requestsRecovery() {
        return false;
    }

    @Override
    public JobBuilder getJobBuilder() {
        return JobBuilder.newJob()
                .ofType(getJobClass())
                .requestRecovery(requestsRecovery())
                .storeDurably(isDurable())
                .usingJobData(getJobDataMap())
                .withDescription(getDescription())
                .withIdentity(getKey());
    }

    @Override
    public Object clone() {
        return null;
    }

}
