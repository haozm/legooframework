package com.legooframework.model.scheduler.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.scheduler.entity.JobDetailBuilderEnity;
import com.legooframework.model.scheduler.entity.SimpleJob;
import com.legooframework.model.scheduler.service.SchedulerJobsFactoryBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/scheduler/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/scheduler/spring-test-cfg.xml"}
)
public class SchedulerSingleFactoryBeanTest {
//    JobDetailFactoryBean
    //  MethodInvokingJobDetailFactoryBean bea;
//    SimpleTriggerFactoryBean
    // CronTriggerFactoryBean
//    SchedulerFactoryBean

    public static void main1(String[] args) throws Exception {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-crm-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/scheduler/spring-model-cfg.xml"
        });

        JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class).withIdentity("jobDetail", "jobDetail").build();

        Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).repeatForever()).build();
        app.getBean("schedulerSingleFactory", Scheduler.class).scheduleJob(jobDetail, trigger);
    }


    public static void main(String[] args) throws Exception {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-crm-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/scheduler/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/scheduler/spring-test-cfg.xml"
        });
//        ScheduleJobService scheduleJobService = app.getBean(ScheduleJobService.class);
//        JobDetailBuilderEnity jb = JobDetailBuilderEnity.createStoreSimple("你好测试", "scheduler", "testJobBean", "clieck",
//                10000L, 21000L, 100098, 1316);
//        LoginContextHolder.setIfNotExitsAnonymousCtx();
//        scheduleJobService.addJob(jb);
    }

    @Test
    public void getObjectType() {
        System.out.println(singleFactory == null);
    }

    @Autowired
    SchedulerJobsFactoryBean singleFactory;
}