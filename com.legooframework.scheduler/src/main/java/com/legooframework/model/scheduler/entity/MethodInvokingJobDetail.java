package com.legooframework.model.scheduler.entity;

import com.google.common.base.Objects;
import org.quartz.*;
import org.quartz.utils.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.JobMethodInvocationFailedException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.InvocationTargetException;

public class MethodInvokingJobDetail extends ArgumentConvertingMethodInvoker implements JobDetail, BeanFactoryAware, InitializingBean {

    private BeanFactory beanFactory;
    private final JobKey jobKey;
    private final String desc;
    private JobDataMap jobDataMap;
    private JobRunParams jobRunParams;
    private final String targetBeanName, targetMethod;

    @Nullable
    private ClassLoader beanClassLoader = org.springframework.util.ClassUtils.getDefaultClassLoader();

    MethodInvokingJobDetail(JobDetailBuilderEnity bulder) {
        this.jobKey = bulder.getJobKey();
        this.desc = bulder.getDescription();
        this.jobDataMap = new JobDataMap();
        this.targetBeanName = bulder.getTargetBeanName();
        this.targetMethod = bulder.getTargetMethod();
        super.setTargetMethod(this.targetMethod);
    }

    void setJobRunParams(JobRunParams jobRunParams) {
        this.jobRunParams = jobRunParams;
    }

    @Override
    public JobKey getKey() {
        return jobKey;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return MethodInvokingJob.class;
    }

    @Override
    public JobDataMap getJobDataMap() {
        return this.jobDataMap;
    }

    @Override
    public boolean isDurable() {
        return true;
    }

    @Override
    protected Class<?> resolveClassName(String className) throws ClassNotFoundException {
        return org.springframework.util.ClassUtils.forName(className, this.beanClassLoader);
    }

    @Override
    public Class<?> getTargetClass() {
        Class<?> targetClass = super.getTargetClass();
        if (targetClass == null && this.targetBeanName != null) {
            Assert.state(this.beanFactory != null, "BeanFactory must be set when using 'targetBeanName'");
            targetClass = this.beanFactory.getType(this.targetBeanName);
        }
        return targetClass;
    }

    @Override
    public Object getTargetObject() {
        Object targetObject = super.getTargetObject();
        if (targetObject == null && this.targetBeanName != null) {
            Assert.state(this.beanFactory != null, "BeanFactory must be set when using 'targetBeanName'");
            targetObject = this.beanFactory.getBean(this.targetBeanName);
        }
        return targetObject;
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
        MethodInvokingJobDetail clone;
        try {
            clone = (MethodInvokingJobDetail) super.clone();
            clone.setBeanFactory(this.beanFactory);
            if (this.jobDataMap != null) {
                clone.jobDataMap = (JobDataMap) jobDataMap.clone();
            }
        } catch (CloneNotSupportedException ex) {
            throw new IncompatibleClassChangeError("Not Cloneable.");
        }
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInvokingJobDetail that = (MethodInvokingJobDetail) o;
        return Objects.equal(jobKey, that.jobKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jobKey);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.setArguments(this.jobRunParams);
        prepare();
        this.getJobDataMap().put("methodInvoker", this);
    }

    public static class MethodInvokingJob extends QuartzJobBean {

        @Nullable
        private MethodInvoker methodInvoker;

        /**
         * Set the MethodInvoker to use.
         */
        public void setMethodInvoker(MethodInvoker methodInvoker) {
            this.methodInvoker = methodInvoker;
        }

        /**
         * Invoke the method via the MethodInvoker.
         */
        @Override
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            Assert.state(this.methodInvoker != null, "No MethodInvoker set");
            try {
                context.setResult(this.methodInvoker.invoke());
            } catch (InvocationTargetException ex) {
                if (ex.getTargetException() instanceof JobExecutionException) {
                    // -> JobExecutionException, to be logged at info level by Quartz
                    throw (JobExecutionException) ex.getTargetException();
                } else {
                    // -> "unhandled exception", to be logged at error level by Quartz
                    throw new JobMethodInvocationFailedException(this.methodInvoker, ex.getTargetException());
                }
            } catch (Exception ex) {
                // -> "unhandled exception", to be logged at error level by Quartz
                throw new JobMethodInvocationFailedException(this.methodInvoker, ex);
            }
        }
    }
}
