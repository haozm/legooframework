package com.legooframework.model.autotask.service;

import com.legooframework.model.autotask.entity.TaskExecuteEntity;
import com.legooframework.model.autotask.entity.TaskExecuteEntityAction;
import com.legooframework.model.autotask.entity.TaskExecuteJobDetailBuilder;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-autotask-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/autotask/spring-model-cfg.xml"}
)
public class AutoTaskServiceTest {

    @Test
    public void buildTaskExecutes() {
        autoTaskService.buildTaskExecutes();
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext app = new ClassPathXmlApplicationContext(ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-reactor-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/autotask/spring-model-cfg.xml");
        LoginContextHolder.setAnonymousCtx();
        Optional<TaskExecuteEntity> task = app.getBean(TaskExecuteEntityAction.class).findById(13);
        TaskExecuteJobDetailBuilder detail = app.getBean(TaskExecuteJobDetailBuilder.class);
        detail.setTaskExecute(task.get());
        JobDetail dd = detail.getJobBuilder().build();
        Trigger tt = TriggerBuilder.newTrigger()
                .withIdentity(String.format("Trigger:%s", "q2312123123"))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 3 22 15 10 ? 2019"))
                .build();
        app.getBean("autotaskJobsScheduler", Scheduler.class).scheduleJob(dd, tt);
    }

    @Autowired
    private AutoTaskService autoTaskService;
}