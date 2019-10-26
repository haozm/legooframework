package com.legooframework.model.scheduler.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.osgi.BundleRuntimeFactory;
import com.legooframework.model.scheduler.entity.JobDetailBuilderEnity;
import com.legooframework.model.scheduler.entity.JobDetailBuilderEnityAction;
import com.legooframework.model.scheduler.entity.TriggerType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ScheduleJobService extends BundleService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleJobService.class);

    /**
     * 天启四骑士
     *
     * @param builder 骑士老大
     */
    public void addJob(JobDetailBuilderEnity builder) {
        String job_key = jobBuilderAction.addNewJob(builder);
        String[] args = StringUtils.split(job_key, "||");
        Optional<JobDetailBuilderEnity> exits_opt = jobBuilderAction.findByJobKey(args[0], args[1]);
        exits_opt.ifPresent(this::restartScheduleJob);
    }

    public void changeTrige(String jobName, String groupName, TriggerType triggerType, String cronExpression,
                            long repeatInterval) {
        Optional<String> job_name_opt = jobBuilderAction.changeTrige(jobName, groupName, triggerType,
                cronExpression, repeatInterval);
        Optional<JobDetailBuilderEnity> builder = jobBuilderAction.findByJobKey(jobName, groupName);
        builder.ifPresent(this::updateTrigger);
    }

    public void pauseJob(String jobName, String groupName) {
        Optional<JobDetailBuilderEnity> exits_opt = jobBuilderAction.findByJobKey(jobName, groupName);
        exits_opt.ifPresent(jb -> {
            JobKey jobKey = new JobKey(jobName, groupName);
            try {
                if (scheduler.checkExists(jobKey)) scheduler.pauseJob(jobKey);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void resumeJob(String jobName, String groupName) {
        Optional<JobDetailBuilderEnity> exits_opt = jobBuilderAction.findByJobKey(jobName, groupName);
        exits_opt.ifPresent(jb -> {
            JobKey jobKey = new JobKey(jobName, groupName);
            try {
                if (scheduler.checkExists(jobKey)) scheduler.resumeJob(jobKey);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 雷神致死
     *
     * @param jobName 喵喵锤
     */
    public void diabledJob(String jobName, String groupName) {
        Optional<JobDetailBuilderEnity> builder_opt = jobBuilderAction.disabled(jobName, groupName);
        builder_opt.ifPresent(this::restartScheduleJob);
    }

    /**
     * 雷神致死
     *
     * @param jobName 喵喵锤
     */
    public void enabled(String jobName, String groupName) {
        Optional<JobDetailBuilderEnity> builder_opt = jobBuilderAction.enabled(jobName, groupName);
        builder_opt.ifPresent(builder -> {
            if (builder.isOwnerBundle(this.bunldeNames))
                restartScheduleJob(builder);
        });
    }

    private void restartScheduleJob(JobDetailBuilderEnity builder) {
        try {
            deleteJobByGrouName(builder);
            Optional<List<JobDetailBuilderEnity>> enabled_list = jobBuilderAction.loadEnabledJobWithGroupName(builder);
            Preconditions.checkState(enabled_list.isPresent(), "异常的数据，缺失新增的任务...");
            for (JobDetailBuilderEnity jb : enabled_list.get()) {
                JobDetail jobDetail = jb.buildJobDetail(appCtx, enabled_list.get());
                Trigger trigger = jb.getTrigger();
                scheduler.scheduleJob(jobDetail, trigger);
                if (logger.isInfoEnabled())
                    logger.info(String.format("Run job %s is success...", jb));
            }
        } catch (Exception e) {
            logger.error(String.format("Run JobDetail has error ... by %s", builder), e);
        }
    }

    private void scheduleJob(JobDetailBuilderEnity builder) {
        try {
            Optional<List<JobDetailBuilderEnity>> enabled_list = jobBuilderAction.loadEnabledJobWithGroupName(builder);
            JobDetail jobDetail = builder.buildJobDetail(appCtx, enabled_list.orElse(null));
            scheduler.scheduleJob(jobDetail, builder.getTrigger());
            if (logger.isInfoEnabled())
                logger.info(String.format("Run job %s is success...", builder));
        } catch (Exception e) {
            logger.error(String.format("Run JobDetail has error ... by %s", builder), e);
        }
    }

    private void deleteJobByGrouName(JobDetailBuilderEnity builder) throws Exception {
        GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(builder.getGroupName());
        Set<JobKey> jobKeys = scheduler.getJobKeys(groupMatcher);
        if (CollectionUtils.isNotEmpty(jobKeys)) {
            jobKeys.forEach(jobkey -> {
                try {
                    if (scheduler.checkExists(jobkey)) {
                        scheduler.deleteJob(jobkey);
                        if (logger.isDebugEnabled())
                            logger.debug(String.format("Delete Job By jobKey = %s", jobkey));
                    }
                } catch (SchedulerException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void updateTrigger(JobDetailBuilderEnity builder) {
        try {
            boolean isrunning = scheduler.checkExists(builder.getJobKey());
            if (isrunning) {
                scheduler.rescheduleJob(builder.getTriggerKey(), builder.getTrigger());
                if (logger.isInfoEnabled())
                    logger.info(String.format("updateTrigger %s is success...", builder));
            }
        } catch (Exception e) {
            logger.error(String.format("updateTrigger JobDetail has error ... by %s", builder), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.bunldeNames = bundleRuntime.getBundleNames();
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<List<JobDetailBuilderEnity>> enabled_job_bulders;
        try {
            enabled_job_bulders = jobBuilderAction.loadEnabledJobWithBundle(this.bunldeNames);
            if (logger.isDebugEnabled())
                logger.debug(String.format("Init entities By bundles is %s", this.bunldeNames));
        } finally {
            LoginContextHolder.clear();
        }
        enabled_job_bulders.ifPresent(builders -> builders.forEach(this::scheduleJob));
    }

    private Scheduler scheduler;
    private BundleRuntimeFactory bundleRuntime;
    private Collection<String> bunldeNames;

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setBundleRuntime(BundleRuntimeFactory bundleRuntime) {
        this.bundleRuntime = bundleRuntime;
    }

    private JobDetailBuilderEnityAction jobBuilderAction;

    public void setJobBuilderAction(JobDetailBuilderEnityAction jobBuilderAction) {
        this.jobBuilderAction = jobBuilderAction;
    }
}
