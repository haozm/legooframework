package com.legooframework.model.autotask.step;

import com.legooframework.model.autotask.entity.DelayType;
import com.legooframework.model.autotask.entity.TaskExecuteEntity;
import com.legooframework.model.autotask.entity.TaskExecuteJobDetailBuilder;
import com.legooframework.model.autotask.service.BundleService;
import com.legooframework.model.core.utils.AppCtxSupport;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

public class TaskExecuteItemProcessor extends AppCtxSupport
        implements ItemProcessor<TaskExecuteEntity, TaskExecuteEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteItemProcessor.class);


    public TaskExecuteItemProcessor() {
    }

    @Override
    public TaskExecuteEntity process(TaskExecuteEntity item) throws Exception {
        TaskExecuteEntity res = null;
        try {
            if (DelayType.NO_DELAY == item.getDelayType()) {
                Message<TaskExecuteEntity> message = MessageBuilder.withPayload(item).build();
                getMessagingTemplate().send(BundleService.CHANNEL_TASK_RUNING, message);
                res = item.setRuning();
            } else if (DelayType.isDelay4Job(item.getDelayType())) {
                try {
                    scheduleJob(item);
                    res = item.setJobQueue();
                } catch (SchedulerException e) {
                    res = item.setError(e.getMessage());
                }
            } else {
                res = item.setError(String.format("非法的任务执行类型 DelayType = %s", item.getDelayType()));
            }
        } catch (Exception e) {
            logger.error(String.format("(%s) has error...", item), e);
            res = item.setError(e.getMessage());
        }
        return res;
    }

    private void scheduleJob(TaskExecuteEntity task) throws SchedulerException {
        TaskExecuteJobDetailBuilder jobDetailBuilder =
                getBean("autoTaskExecuteJobDetail", TaskExecuteJobDetailBuilder.class);
        jobDetailBuilder.setTaskExecute(task);
        JobDetail jobDetail = jobDetailBuilder.getJobBuilder().build();
        Trigger trigger = task.createTrigger();
        getScheduler().scheduleJob(jobDetail, trigger);
    }

    // ---------------------------------- setter ----------------------------------
    MessagingTemplate getMessagingTemplate() {
        return getBean("autoTaskMessagingTemplate", MessagingTemplate.class);
    }

    Scheduler getScheduler() {
        return getBean("autotaskJobsScheduler", Scheduler.class);
    }

}
