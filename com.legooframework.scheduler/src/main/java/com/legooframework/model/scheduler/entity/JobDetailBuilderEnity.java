package com.legooframework.model.scheduler.entity;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class JobDetailBuilderEnity extends BaseEntity<Long> {

    private static final Logger logger = LoggerFactory.getLogger(JobDetailBuilderEnity.class);

    private final String groupName, targetBeanName, targetMethod;
    private final Integer companyId, storeId;
    private boolean enabled;
    private TriggerType triggerType;
    private long startDelay, repeatInterval;
    private String jobName, jobDesc, ownerBundle;
    private CronExpression cronExpression;
    private JobKey jobKey;
    private Trigger trigger;
    private TriggerKey triggerKey;

    Optional<JobDetailBuilderEnity> changeTrige(TriggerType triggerType, String cronExpression, long repeatInterval) {
        if (TriggerType.CronTrigger == triggerType) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(cronExpression), "参数 cronExpression 不可以为空值");
            if (this.triggerType == triggerType && StringUtils.equals(cronExpression, this.cronExpression.toString()))
                return Optional.empty();
            JobDetailBuilderEnity clone = (JobDetailBuilderEnity) cloneMe();
            clone.triggerType = triggerType;
            clone.cronExpression = createCron(cronExpression);
            clone.repeatInterval = 0;
            return Optional.of(clone);
        } else if (TriggerType.SimpleTrigger == this.triggerType) {
            Preconditions.checkArgument(repeatInterval > 0, "参数 repeatInterval 不可以为空值");
            if (this.triggerType == triggerType && repeatInterval == this.repeatInterval)
                return Optional.empty();
            JobDetailBuilderEnity clone = (JobDetailBuilderEnity) cloneMe();
            clone.triggerType = triggerType;
            clone.cronExpression = null;
            clone.repeatInterval = repeatInterval;
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    private CronExpression createCron(String cronExpression) {
        try {
            return new CronExpression(cronExpression);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("非法的cronExpression=%s 表达式...", cronExpression));
        }
    }

    public static JobDetailBuilderEnity createGeneralCron(String jobDesc, String ownerBundle, String targetBeanName,
                                                          String targetMethod, String cronExpression) {
        return new JobDetailBuilderEnity(jobDesc, ownerBundle, targetBeanName,
                targetMethod, cronExpression, -1, -1);
    }

    public static JobDetailBuilderEnity createCompanyCron(String jobDesc, String ownerBundle, String targetBeanName,
                                                          String targetMethod, String cronExpression, Integer companId) {
        Preconditions.checkArgument(companId != null && companId > 0, "非法的公司ID=%s", companId);
        return new JobDetailBuilderEnity(jobDesc, ownerBundle, targetBeanName,
                targetMethod, cronExpression, companId, -1);
    }

    public static JobDetailBuilderEnity createStoreCron(String jobDesc, String ownerBundle,
                                                        String targetBeanName,
                                                        String targetMethod, String cronExpression, Integer companId, Integer storeId) {
        Preconditions.checkArgument(companId != null && companId > 0, "非法的公司ID=%s", companId);
        Preconditions.checkArgument(storeId != null && storeId > 0, "非法的门店ID=%s", companId);
        return new JobDetailBuilderEnity(jobDesc, ownerBundle, targetBeanName,
                targetMethod, cronExpression, companId, storeId);
    }

    private JobDetailBuilderEnity(String jobDesc, String ownerBundle, String targetBeanName,
                                  String targetMethod, String cronExpression, Integer companId, Integer storeId) {
        super(0L);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(targetBeanName), "targetBeanName cannot be null...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(targetMethod), "targetMethod cannot be null...");
        this.enabled = true;
        this.ownerBundle = ownerBundle;
        this.jobName = String.format("%s_%s", companId, storeId);
        this.groupName = String.format("%s_%s", targetBeanName, targetMethod);
        this.jobDesc = jobDesc;
        this.targetBeanName = targetBeanName;
        this.targetMethod = targetMethod;
        this.triggerType = TriggerType.CronTrigger;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cronExpression), "cronExpression 不可为空值...");
        setCronExpression(cronExpression);
        this.startDelay = 0;
        this.repeatInterval = 0;
        this.companyId = companId;
        this.storeId = storeId;
    }

    String getOwnerBundle() {
        return ownerBundle;
    }

    public String getGroupName() {
        return groupName;
    }

    private void setCronExpression(String cronExpression) {
        try {
            this.cronExpression = new CronExpression(cronExpression);
        } catch (ParseException e) {
            String msg = String.format("非法的cron 表达式 ： %s", cronExpression);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    boolean isGeneralJob() {
        return this.companyId == -1 && this.storeId == -1;
    }

    boolean isCompanyJob() {
        return this.companyId != -1 && this.storeId == -1;
    }

    boolean isStoreJob() {
        return this.companyId != -1 && this.storeId != -1;
    }

    boolean isStoreJob(Integer companyId) {
        return Objects.equal(this.companyId, companyId) && this.storeId != -1;
    }

    boolean isJobKey(String jobName, String groupName) {
        return StringUtils.equals(this.jobName, jobName) && StringUtils.equals(this.groupName, groupName);
    }

    boolean isTargetMethod(JobDetailBuilderEnity that) {
        return StringUtils.equals(this.targetBeanName, that.targetBeanName)
                && StringUtils.equals(this.targetMethod, that.targetMethod);
    }

    boolean isTargetMethodWithRange(JobDetailBuilderEnity that) {
        return StringUtils.equals(this.targetBeanName, that.targetBeanName)
                && StringUtils.equals(this.targetMethod, that.targetMethod)
                && Objects.equal(this.companyId, that.companyId)
                && Objects.equal(this.storeId, that.storeId);
    }


    public static JobDetailBuilderEnity createGeneralSimple(String jobDesc, String ownerBundle,
                                                            String targetBeanName, String targetMethod,
                                                            long startDelay, long repeatInterval) {
        return new JobDetailBuilderEnity(jobDesc, ownerBundle, targetBeanName,
                targetMethod, startDelay, repeatInterval, -1, -1);
    }

    public static JobDetailBuilderEnity createCompanySimple(String jobDesc, String ownerBundle,
                                                            String targetBeanName, String targetMethod,
                                                            long startDelay, long repeatInterval, Integer companId) {
        Preconditions.checkArgument(companId != null && companId > 0, "非法的公司ID=%s", companId);
        return new JobDetailBuilderEnity(jobDesc, ownerBundle, targetBeanName,
                targetMethod, startDelay, repeatInterval, companId, -1);
    }

    public static JobDetailBuilderEnity createStoreSimple(String jobDesc, String ownerBundle,
                                                          String targetBeanName, String targetMethod,
                                                          long startDelay, long repeatInterval, Integer companId, Integer storeId) {
        Preconditions.checkArgument(companId != null && companId > 0, "非法的公司ID=%s", companId);
        Preconditions.checkArgument(storeId != null && storeId > 0, "非法的门店ID=%s", companId);
        return new JobDetailBuilderEnity(jobDesc, ownerBundle, targetBeanName,
                targetMethod, startDelay, repeatInterval, companId, storeId);
    }


    private JobDetailBuilderEnity(String jobDesc, String ownerBundle, String targetBeanName,
                                  String targetMethod, long startDelay, long repeatInterval, Integer companId, Integer storeId) {
        super(0L);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(targetBeanName), "targetBeanName cannot be null...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(targetMethod), "targetMethod cannot be null...");
        Preconditions.checkArgument(repeatInterval > 0, "repeatInterval =%s 非法...", repeatInterval);
        this.enabled = true;
        this.targetBeanName = targetBeanName;
        this.targetMethod = targetMethod;
        this.ownerBundle = ownerBundle;
        this.jobName = String.format("%s_%s", companId, storeId);
        this.groupName = String.format("%s_%s", targetBeanName, targetMethod);
        this.jobDesc = jobDesc;
        this.startDelay = startDelay < 0 ? 0 : startDelay;
        this.triggerType = TriggerType.SimpleTrigger;
        this.repeatInterval = repeatInterval;
        this.cronExpression = null;
        this.companyId = companId;
        this.storeId = storeId;
    }

    public JobKey getJobKey() {
        return this.jobKey;
    }

    public TriggerKey getTriggerKey() {
        return triggerKey;
    }

    String getDescription() {
        return String.format("%s->%s.%s", this.jobKey, this.targetBeanName, this.targetMethod);
    }

    public boolean isEnabled() {
        return enabled;
    }

    Optional<JobDetailBuilderEnity> disabled() {
        if (isEnabled()) {
            JobDetailBuilderEnity clone = (JobDetailBuilderEnity) cloneMe();
            clone.enabled = false;
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    String getJobName() {
        return jobName;
    }

    Optional<JobDetailBuilderEnity> enbaled() {
        if (isEnabled()) return Optional.empty();
        JobDetailBuilderEnity clone = (JobDetailBuilderEnity) cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    public MethodInvokingJobDetail buildJobDetail(BeanFactory beanFactory, List<JobDetailBuilderEnity> jobEnabledList) {
        MethodInvokingJobDetail jobDetail = new MethodInvokingJobDetail(this);
        jobDetail.setBeanFactory(beanFactory);
        JobRunParams jobRunParams = new JobRunParams(this.companyId, this.storeId);
        if (CollectionUtils.isNotEmpty(jobEnabledList)) {
            jobEnabledList.forEach(job -> job.isTargetMethod(this));
            if (isGeneralJob()) {
                List<JobDetailBuilderEnity> com_list = jobEnabledList.stream().filter(JobDetailBuilderEnity::isCompanyJob)
                        .filter(JobDetailBuilderEnity::isEnabled).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(com_list))
                    com_list.forEach(x -> jobRunParams.setExcludeComs(x.companyId));
                List<JobDetailBuilderEnity> store_list = jobEnabledList.stream().filter(JobDetailBuilderEnity::isStoreJob)
                        .filter(JobDetailBuilderEnity::isEnabled).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(store_list))
                    store_list.forEach(x -> jobRunParams.setExcludeStore(x.companyId, x.storeId));
            } else if (isCompanyJob()) {
                List<JobDetailBuilderEnity> store_list = jobEnabledList.stream().filter(x -> x.isStoreJob(this.companyId))
                        .filter(JobDetailBuilderEnity::isEnabled).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(store_list))
                    store_list.forEach(x -> jobRunParams.setExcludeStore(this.companyId, x.storeId));
            }
        }
        jobDetail.setJobRunParams(jobRunParams);
        try {
            jobDetail.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(String.format("createJobDetail(beanFactory) has error by %s", this));
            throw new RuntimeException(e);
        }
        return jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    String getTargetBeanName() {
        return targetBeanName;
    }

    String getTargetMethod() {
        return targetMethod;
    }

    public boolean isOwnerBundle(Collection<String> bundleNames) {
        return CollectionUtils.isNotEmpty(bundleNames) && bundleNames.contains(this.ownerBundle);
    }

    JobDetailBuilderEnity(Long wxId, ResultSet res) {
        super(wxId, res);
        try {
            this.jobName = res.getString("jobName");
            this.groupName = res.getString("groupName");
            this.jobDesc = res.getString("jobDesc");
            this.companyId = res.getInt("companyId");
            this.storeId = res.getInt("storeId");
            this.ownerBundle = res.getString("ownerBundle");
            this.targetMethod = res.getString("targetMethod");
            this.targetBeanName = res.getString("targetBeanName");
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
            this.triggerType = TriggerType.parse(res.getInt("triggerType"));
            this.jobKey = new JobKey(this.jobName, this.groupName);
            this.triggerKey = new TriggerKey(this.jobName, this.groupName);
            if (TriggerType.SimpleTrigger == this.triggerType) {
                this.startDelay = res.getInt("startDelay");
                this.repeatInterval = res.getInt("repeatInterval");
                Preconditions.checkArgument(this.repeatInterval > 0, "时间间隔需大于0，非法参数 repeatInterval = %s",
                        this.repeatInterval);
                Date startTime = this.startDelay > 0 ? new Date(System.currentTimeMillis() + this.startDelay) :
                        new Date();
                this.trigger = TriggerBuilder.newTrigger().forJob(this.getJobKey()).withIdentity(triggerKey).startAt(startTime)
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(this.repeatInterval)
                                .repeatForever()).build();
            } else {
                setCronExpression(res.getString("cronExpression"));
                this.trigger = TriggerBuilder.newTrigger().forJob(this.getJobKey()).withIdentity(triggerKey)
                        .withSchedule(CronScheduleBuilder.cronSchedule(this.cronExpression)).build();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore JobDetailBuilderEnity has SQLException", e);
        }
    }

    boolean equalsByJobName(JobDetailBuilderEnity that) {
        return Objects.equal(this.jobName, that.jobName);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("enabled", enabled ? 1 : 0);
        params.put("groupName", groupName);
        params.put("jobName", jobName);
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("jobDesc", jobDesc);
        params.put("ownerBundle", ownerBundle);
        params.put("cronExpression", TriggerType.CronTrigger == this.triggerType ? cronExpression.toString() : null);
        params.put("targetBeanName", targetBeanName);
        params.put("targetMethod", targetMethod);
        params.put("startDelay", startDelay);
        params.put("repeatInterval", repeatInterval);
        params.put("triggerType", triggerType.getType());
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobDetailBuilderEnity that = (JobDetailBuilderEnity) o;
        return enabled == that.enabled &&
                startDelay == that.startDelay &&
                repeatInterval == that.repeatInterval &&
                Objects.equal(groupName, that.groupName) &&
                triggerType == that.triggerType &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(jobName, that.jobName) &&
                Objects.equal(jobDesc, that.jobDesc) &&
                Objects.equal(ownerBundle, that.ownerBundle) &&
                Objects.equal(cronExpression, that.cronExpression) &&
                Objects.equal(targetBeanName, that.targetBeanName) &&
                Objects.equal(targetMethod, that.targetMethod) &&
                Objects.equal(jobKey, that.jobKey) &&
                Objects.equal(trigger, that.trigger) &&
                Objects.equal(triggerKey, that.triggerKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(groupName, companyId, storeId, enabled, triggerType, startDelay, repeatInterval,
                jobName, jobDesc, cronExpression, targetBeanName, ownerBundle, targetMethod, jobKey, trigger, triggerKey);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("groupName", groupName)
                .add("enabled", enabled)
                .add("triggerType", triggerType)
                .add("startDelay", startDelay)
                .add("repeatInterval", repeatInterval)
                .add("ownerBundle", ownerBundle)
                .add("jobName", jobName)
                .add("jobDesc", jobDesc)
                .add("cronExpression", cronExpression)
                .add("targetBeanName", targetBeanName)
                .add("targetMethod", targetMethod)
                .add("jobKey", jobKey)
                .add("trigger", trigger)
                .add("triggerKey", triggerKey)
                .toString();
    }
}
