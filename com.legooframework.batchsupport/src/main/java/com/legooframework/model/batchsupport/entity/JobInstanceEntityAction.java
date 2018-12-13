package com.legooframework.model.batchsupport.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JobInstanceEntityAction extends BaseEntityAction<JobInstanceEntity> {
    private static final Logger logger = LoggerFactory.getLogger(JobInstanceEntityAction.class);

    public JobInstanceEntityAction() {
        super(null);
    }

    public List<String> loadAllJobInstance() {
        List<String> jobNames = jobExplorer.getJobNames();
        if (logger.isTraceEnabled())
            logger.trace(String.format("loadAllJob is %s", jobNames));
        return jobNames;
    }

    private Map<String, Object> handleParams(String jobName, Integer companyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "JobName 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("jobName", jobName);
        if (companyId != null) params.put("companyId", companyId);
        return params;
    }

    public Optional<JobExecution> getLastJobExecution(String jobName, Integer companyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "JobName 不可以为空值...");
        Map<String, Object> params = handleParams(jobName, companyId);
        Optional<Long> instanceId = super.queryForObject("getLastJobExecution", params, Long.class);
        if (instanceId.isPresent()) {
            JobExecution jobExecution = jobExplorer.getJobExecution(instanceId.get());
            return Optional.ofNullable(jobExecution);
        }
        return Optional.empty();
    }

    public Optional<JobInstance> getLastJobInstance(String jobName, Integer companyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "JobName 不可以为空值...");
        Map<String, Object> params = handleParams(jobName, companyId);
        Optional<Long> instanceId = super.queryForObject("getLastJobInstance", params, Long.class);
        if (instanceId.isPresent()) {
            JobInstance jobInstance = jobExplorer.getJobInstance(instanceId.get());
            return Optional.ofNullable(jobInstance);
        }
        return Optional.empty();
    }

    private JobExplorer jobExplorer;

    public void setJobExplorer(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    @Override
    protected RowMapper<JobInstanceEntity> getRowMapper() {
        return null;
    }

}
