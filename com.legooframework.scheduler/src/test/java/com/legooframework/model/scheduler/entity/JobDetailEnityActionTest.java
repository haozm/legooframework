package com.legooframework.model.scheduler.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobDetail;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-crm-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/scheduler/spring-model-cfg.xml"}
)
public class JobDetailEnityActionTest implements ApplicationContextAware {

    @Test
    public void addNewJob() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
//        String jobName, String jobDesc, String targetBeanName, String targetMethod, long startDelay,
//        long repeatInterval
        JobDetailBuilderEnity job = JobDetailBuilderEnity.createGeneralSimple("测试任务", "schedulerBundle",
                "springBean", "runMe", 5000L, 5000L);
        jobDetailEnityAction.addNewJob(job);
    }

    @Test
    public void disabled() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<JobDetailBuilderEnity> builder = jobDetailEnityAction.findByJobKey("-1_-1", "testJobBean_clieck");
        Optional<List<JobDetailBuilderEnity>> builders = jobDetailEnityAction.loadEnabledJobs();
        if (builder.isPresent()) {
            List<JobDetailBuilderEnity> sun_list = builders.get().stream().filter(x -> x.isTargetMethod(builder.get()))
                    .collect(Collectors.toList());
            JobDetail asd = builder.get().buildJobDetail(appCtx, sun_list);
            System.out.println("nihao");
        }
    }


    @Test
    public void changeTrige() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        jobDetailEnityAction.changeTrige("testJob", null, TriggerType.SimpleTrigger, null, 10000);
    }

    @Test
    public void enabled() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        jobDetailEnityAction.enabled("testJob", null);
    }

    @Test
    public void addNewJob2() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
//        String jobName, String jobDesc, String targetBeanName, String targetMethod, long startDelay,
//        long repeatInterval
        JobDetailBuilderEnity job = JobDetailBuilderEnity.createStoreCron("测试任务", "schedulerBundle", "springBean",
                "clieck", "0 0 0/1 * * ? *", 10098, 1314);
        jobDetailEnityAction.addNewJob(job);
        JobDetailBuilderEnity job1 = JobDetailBuilderEnity.createCompanyCron("测试任务", "schedulerBundle", "springBean",
                "clieck", "0 0 0/1 * * ? *", 100981);
        jobDetailEnityAction.addNewJob(job1);
        JobDetailBuilderEnity job2 = JobDetailBuilderEnity.createGeneralCron("测试任务", "schedulerBundle", "springBean",
                "clieck", "0 0 0/1 * * ? *");
        jobDetailEnityAction.addNewJob(job2);
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    ApplicationContext appCtx;

    @Autowired
    private JobDetailBuilderEnityAction jobDetailEnityAction;
}
