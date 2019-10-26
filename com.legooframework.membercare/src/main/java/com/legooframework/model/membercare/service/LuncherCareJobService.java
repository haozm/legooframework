package com.legooframework.model.membercare.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.membercare.entity.BusinessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.util.Map;
import java.util.Optional;

public class LuncherCareJobService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(LuncherCareJobService.class);

    void runJob(BusinessType businessType, JobParameters parameters) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isInfoEnabled())
            logger.info(String.format("Run Job Store(%s) by jobParams %s", businessType, parameters));
        try {
            Optional<Map<String, Job>> jobs = getBeanMap(Job.class);
            Preconditions.checkState(jobs.isPresent(), "当前没有配置Job任务...");
            Preconditions.checkState(jobs.get().containsKey(businessType.getJobName()),
                    "当前任务尚未配置 %s 对应的任务...", businessType);
            Job exec_job = getBean(businessType.getJobName(), Job.class);
            try {
                getBean("jobLauncher", JobLauncher.class).run(exec_job, parameters);
            } catch (JobExecutionAlreadyRunningException e) {
                logger.error(String.format("%s has JobExecutionAlreadyRunningException which is %s", businessType, parameters));
                throw new RuntimeException(e);
            } catch (JobRestartException e) {
                logger.error(String.format("%s has JobRestartException which is %s", businessType, parameters));
                throw new RuntimeException(e);
            } catch (JobInstanceAlreadyCompleteException e) {
                logger.error(String.format("%s has JobInstanceAlreadyCompleteException which is %s", businessType, parameters));
                throw new RuntimeException(e);
            } catch (JobParametersInvalidException e) {
                logger.error(String.format("%s has JobParametersInvalidException which is %s", businessType, parameters));
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            logger.error(String.format("Run Job Store(%s) by jobParams %s has error", businessType, parameters), e);
        } finally {
            LoginContextHolder.clear();
        }
    }

}
