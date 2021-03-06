package com.legooframework.model.batchsupport.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class JobInstanceEntityAction extends BaseEntityAction<JobInstanceEntity> {

    private static final Logger logger = LoggerFactory.getLogger(JobInstanceEntityAction.class);

    public JobInstanceEntityAction() {
        super(null);
    }

    public List<String> loadAllJobInstance() {
        List<String> jobNames = jobExplorer.getJobNames();
        if (logger.isTraceEnabled())
            logger.trace(String.format("loadAllJobInstance is %s", jobNames));
        return jobNames;
    }

    private Map<String, Object> handleParams(String jobName, Integer companyId, Integer storeId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "JobName 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("jobName", jobName);
        if (companyId != null) params.put("companyId", companyId);
        if (storeId != null) params.put("storeId", storeId);
        return params;
    }

    public Optional<JobExecution> loadLastJobExecution(String jobName, Integer companyId, Integer storeId,
                                                       String categories) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "JobName 不可以为空值...");
        String jobParams = String.format("companyId=%s,storeId=%s,categories=%s", companyId, storeId, categories);
        Map<String, Object> params = Maps.newHashMap();
        params.put("jobName", jobName);
        params.put("jobParams", jobParams);
        params.put("sql", "loadLastTouch90JobExecution");
        Optional<Long> instanceId = super.queryForSimpleObj("loadLastJobExecution", params, Long.class);
        if (instanceId.isPresent()) {
            JobExecution jobExecution = jobExplorer.getJobExecution(instanceId.get());
            return Optional.ofNullable(jobExecution);
        }
        return Optional.empty();
    }

    public Optional<JobInstance> getLastJobInstance(String jobName, Integer companyId, Integer storeId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "JobName 不可以为空值...");
        Map<String, Object> params = handleParams(jobName, companyId, storeId);
        Optional<Long> instanceId = super.queryForSimpleObj("getLastJobInstance", params, Long.class);
        if (instanceId.isPresent()) {
            JobInstance jobInstance = jobExplorer.getJobInstance(instanceId.get());
            return Optional.ofNullable(jobInstance);
        }
        return Optional.empty();
    }

    public void disableJobByParams(String jobName, Collection<String[]> jobParams) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("jobName", jobName);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(jobParams), "定位Job的入参不可以为空值...");
        params.put("jobParams", jobParams);
        params.put("sql", "disableJobByParams");
        super.updateAction("disableJobByParams", params);
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
