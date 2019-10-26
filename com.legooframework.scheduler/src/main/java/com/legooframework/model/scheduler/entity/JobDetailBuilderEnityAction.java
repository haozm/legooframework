package com.legooframework.model.scheduler.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JobDetailBuilderEnityAction extends BaseEntityAction<JobDetailBuilderEnity> {

    private static final Logger logger = LoggerFactory.getLogger(JobDetailBuilderEnityAction.class);

    private static final Comparator<JobDetailBuilderEnity> JOB_COMPARATOR = Comparator
            .comparingInt(job -> job.isGeneralJob() ? 0 : job.isCompanyJob() ? 1 : 2);

    public JobDetailBuilderEnityAction() {
        super("JobDetailBuilderCache");
    }

    public String addNewJob(JobDetailBuilderEnity jobDetail) {
        Optional<List<JobDetailBuilderEnity>> exits_list = findAllJobs();
        if (exits_list.isPresent()) {
            Optional<JobDetailBuilderEnity> exits = exits_list.get().stream()
                    .filter(x -> x.isTargetMethodWithRange(jobDetail)).findFirst();
            Preconditions.checkState(!exits.isPresent(), "该任务定义已经存在...%s", jobDetail);
        }
        super.updateAction(jobDetail, "insert");
        getCache().ifPresent(c -> c.evict(String.format("%s_all_jobs", getModelName())));
        return String.format("%s||%s", jobDetail.getJobName(), jobDetail.getGroupName());
    }

    @Override
    public Optional<JobDetailBuilderEnity> findById(Object id) {
        throw new UnsupportedOperationException("不支持该方法调用");
    }

    public Optional<JobDetailBuilderEnity> findByJobKey(String jobName, String groupName) {
        Optional<List<JobDetailBuilderEnity>> all_jobs = findAllJobs();
        return all_jobs.flatMap(c -> c.stream().filter(x -> x.isJobKey(jobName, groupName)).findFirst());

    }

    public Optional<JobDetailBuilderEnity> disabled(String jobName, String groupName) {
        Optional<JobDetailBuilderEnity> enity_opt = findByJobKey(jobName, groupName);
        if (enity_opt.isPresent()) {
            Optional<JobDetailBuilderEnity> disabled = enity_opt.get().disabled();
            disabled.ifPresent(d -> {
                super.updateAction(d, "disabledOrEnabled");
                getCache().ifPresent(c -> c.evict(String.format("%s_all_jobs", getModelName())));
            });
            return disabled;
        }
        return Optional.empty();
    }

    /**
     * 激活 任务
     *
     * @param jobName 待激活的任务名称
     * @return Optional
     */
    public Optional<JobDetailBuilderEnity> enabled(String jobName, String groupName) {
        Optional<JobDetailBuilderEnity> enity_opt = findByJobKey(jobName, groupName);
        if (enity_opt.isPresent()) {
            List<JobDetailBuilderEnity> exits_list = loadAllJobs();
            List<JobDetailBuilderEnity> sub_list = exits_list.stream().filter(x -> x.isTargetMethodWithRange(enity_opt.get()))
                    .collect(Collectors.toList());
            Preconditions.checkState(sub_list.size() == 1, "存在多个同类型的任务...请检查...");
            Optional<JobDetailBuilderEnity> enabled = enity_opt.get().enbaled();
            enabled.ifPresent(d -> {
                super.updateAction(d, "disabledOrEnabled");
                getCache().ifPresent(c -> c.evict(String.format("%s_all_jobs", getModelName())));
            });
            return enabled;
        }
        return Optional.empty();
    }

    /**
     * 变更任务触发器 修改执行时间
     *
     * @param jobName        任务名称
     * @param triggerType    类型
     * @param cronExpression 表达式
     * @param repeatInterval 时间家呢
     * @return 你的
     */
    public Optional<String> changeTrige(String jobName, String groupName, TriggerType triggerType, String cronExpression,
                                        long repeatInterval) {
        Optional<JobDetailBuilderEnity> exits_opt = findByJobKey(jobName, groupName);
        Preconditions.checkState(exits_opt.isPresent(), "不存在jobName=%s 对应的任务定义...", jobName);
        Optional<JobDetailBuilderEnity> clone = exits_opt.get().changeTrige(triggerType, cronExpression, repeatInterval);
        if (clone.isPresent()) {
            super.updateAction(clone.get(), "changeTrige");
            getCache().ifPresent(c -> c.evict(String.format("%s_all_jobs", getModelName())));
            return Optional.of(jobName);
        }
        return Optional.empty();
    }

    /**
     * 加载激活可使用的任务明细
     *
     * @return 我的妩媚
     */
    public Optional<List<JobDetailBuilderEnity>> loadEnabledJobWithBundle(Collection<String> bundleName) {
        Optional<List<JobDetailBuilderEnity>> list_all_opt = loadEnabledJobs();
        if (!list_all_opt.isPresent()) return Optional.empty();
        List<JobDetailBuilderEnity> enabled_list = list_all_opt.get().stream()
                .filter(x -> bundleName.contains(x.getOwnerBundle())).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(enabled_list) ? null : enabled_list);
    }

    /**
     * 加载激活可使用的任务明细
     *
     * @return 我的妩媚
     */
    public Optional<List<JobDetailBuilderEnity>> loadEnabledJobWithGroupName(JobDetailBuilderEnity jobDetail) {
        Optional<List<JobDetailBuilderEnity>> list_all_opt = loadEnabledJobs();
        if (!list_all_opt.isPresent()) return Optional.empty();
        List<JobDetailBuilderEnity> enabled_list = list_all_opt.get().stream()
                .filter(x -> StringUtils.equals(jobDetail.getGroupName(), x.getGroupName())).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(enabled_list) ? null : enabled_list);
    }

    /**
     * 加载激活可使用的任务明细
     *
     * @return 我的妩媚
     */
    public Optional<List<JobDetailBuilderEnity>> loadEnabledJobs() {
        Optional<List<JobDetailBuilderEnity>> list_all_opt = findAllJobs();
        if (!list_all_opt.isPresent()) return Optional.empty();
        List<JobDetailBuilderEnity> enabled_list = list_all_opt.get().stream().filter(JobDetailBuilderEnity::isEnabled)
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(enabled_list) ? null : enabled_list);
    }

    private List<JobDetailBuilderEnity> loadAllJobs() {
        Optional<List<JobDetailBuilderEnity>> jobs = findAllJobs();
        Preconditions.checkState(jobs.isPresent(), "任务为空...");
        return jobs.get();
    }

    private Optional<List<JobDetailBuilderEnity>> findAllJobs() {
        final String cache_key = String.format("%s_all_jobs", getModelName());
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<JobDetailBuilderEnity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        Optional<List<JobDetailBuilderEnity>> list_opt = super.queryForEntities("loadAll", null, getRowMapper());
        list_opt.ifPresent(list -> getCache().ifPresent(c -> c.put(cache_key, list)));
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllJobs() size is %s", list_opt.map(List::size).orElse(0)));
        return list_opt;
    }

    @Override
    protected RowMapper<JobDetailBuilderEnity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<JobDetailBuilderEnity> {
        @Override
        public JobDetailBuilderEnity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new JobDetailBuilderEnity(res.getLong("id"), res);
        }
    }
}
