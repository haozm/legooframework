package com.legooframework.model.batchsupport.service;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.legooframework.model.batchsupport.entity.JobInstanceEntityAction;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class LegooJobService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(LegooJobService.class);

    private final static Joiner STORES_JOINER = Joiner.on(',');

    @Override
    protected Bundle getLocalBundle() {
        return getBean("batchJobBundle", Bundle.class);
    }

    public Optional<JobExecution> getLastJobExecution(String jobName, Integer companyId) {
        return getJobInstanceAction().getLastJobExecution(jobName, companyId);
    }

    public void runJob(String jobName, Long companyId, JobParameters parameters) {
        if (logger.isInfoEnabled())
            logger.info(String.format("Run Job %s which %s by param %s", jobName, companyId, parameters));
        run(jobName, companyId, null, null, parameters);
    }

    public void runJob(String jobName, Long companyId, Collection<Integer> storeIds, JobParameters parameters) {
        if (logger.isInfoEnabled())
            logger.info(String.format("Run Job %s which %s by param %s", jobName, companyId, parameters));
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(storeIds), "门店集合不可以为空...");
        run(jobName, companyId, storeIds, null, parameters);
    }

    public void runJob(String jobName, Long companyId, Long storeId, JobParameters parameters) {
        if (logger.isInfoEnabled())
            logger.info(String.format("Run Job %s which %s by param %s", jobName, companyId, parameters));
        Preconditions.checkArgument(storeId != null, "门店ID 不可以为空值...");
        run(jobName, companyId, null, storeId, parameters);
    }

    private void run(String jobName, Long companyId, Collection<Integer> storeIds, Long storeId, JobParameters parameters) {
        Preconditions.checkNotNull(companyId, "公司ID 不可以为空...");
        Optional<Map<String, Job>> jobs = getBeanMap(Job.class);
        Preconditions.checkState(jobs.isPresent(), "当前没有配置Job任务...");
        Preconditions.checkState(jobs.get().containsKey(jobName), "当前任务尚未配置 %s 对应的任务...", jobName);
        JobParametersBuilder pb = new JobParametersBuilder();
        pb.addLong("companyId", companyId)
                .addString("storeIds", CollectionUtils.isNotEmpty(storeIds) ? STORES_JOINER.join(storeIds) : "ALL")
                .addLong("storeId", storeId == null ? -1L : storeId);
        if (parameters != null) pb.addJobParameters(parameters);
        JobParameters params = pb.toJobParameters();
        Job _run_job = getBean(jobName, Job.class);
        try {
            getJobLauncher().run(_run_job, params);
        } catch (JobExecutionAlreadyRunningException e) {
            logger.error(String.format("%s has JobExecutionAlreadyRunningException which is %s", jobName, params));
            throw new RuntimeException(e);
        } catch (JobRestartException e) {
            logger.error(String.format("%s has JobRestartException which is %s", jobName, params));
            throw new RuntimeException(e);
        } catch (JobInstanceAlreadyCompleteException e) {
            logger.error(String.format("%s has JobInstanceAlreadyCompleteException which is %s", jobName, params));
            throw new RuntimeException(e);
        } catch (JobParametersInvalidException e) {
            logger.error(String.format("%s has JobParametersInvalidException which is %s", jobName, params));
            throw new RuntimeException(e);
        }
    }

    private JobLauncher getJobLauncher() {
        return getBean("legooJobLauncher", JobLauncher.class);
    }

    private JobInstanceEntityAction getJobInstanceAction() {
        return getBean(JobInstanceEntityAction.class);
    }
}
