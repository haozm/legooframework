package com.legooframework.model.autotask.entity;

import com.legooframework.model.autotask.service.BundleService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;

public class TaskExecuteJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteJob.class);

    public TaskExecuteJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        TaskExecuteEntity task = (TaskExecuteEntity) data.get(TaskExecuteEntity.class.getSimpleName());
        MessagingTemplate messagingTemplate = (MessagingTemplate) data.get(MessagingTemplate.class.getSimpleName());
        if (logger.isDebugEnabled())
            logger.debug(String.format("TaskExecuteJob.execute(%s)", task));
        try {
            messagingTemplate.send(BundleService.CHANNEL_TASK_RUNING, MessageBuilder.withPayload(task).build());
        } catch (Exception e) {
            String errmsg = String.format("messagingTemplate.send(%s,%s) has error", BundleService.CHANNEL_TASK_RUNING, task);
            logger.error(errmsg, e);
            throw new JobExecutionException(errmsg);
        }
    }
}
