package com.legooframework.model.scheduler.entity;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MethodInvokingJobDetailFactory implements ApplicationContextAware {

    private ApplicationContext appCtx;

    public final JobDetail createJobDetail(JobDetailBuilderEnity builder) throws Exception {
        MethodInvokingJobDetail jobDetail = new MethodInvokingJobDetail(builder);
        jobDetail.setBeanFactory(appCtx);
        jobDetail.afterPropertiesSet();
        return jobDetail;
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }
}
