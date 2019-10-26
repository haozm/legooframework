package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpcomingTaskAutoRunDto {

    private final TaskCareRule4Touch90Entity rule;
    private final List<UpcomingTaskDetailEntity> taskDetails;
    private List<MemberInfo> memberInfos;
    private List<UpcomingTaskEntity> upcomingTasks;
    private boolean error = false;

    public UpcomingTaskAutoRunDto(TaskCareRule4Touch90Entity rule, List<UpcomingTaskDetailEntity> taskDetails) {
        this.rule = rule;
        this.taskDetails = taskDetails;
        this.memberInfos = Lists.newArrayList();
        this.upcomingTasks = Lists.newArrayList();
    }

    public Optional<List<UpcomingTaskDetailExecDto>> filterRunEnabledList() {
        Preconditions.checkState(rule.getAutoRunChannel().isPresent());
        List<UpcomingTaskDetailEntity> run_enbaled_list = Lists.newArrayList();
        if (isSingleSalesRange()) {
            Preconditions.checkState(rule.getSingleSalesAmount().isPresent());
            Range<Integer> range = rule.getSingleSalesAmount().get();
            this.taskDetails.forEach(x -> {
                this.upcomingTasks.stream().filter(c -> Objects.equal(x.getTaskId(), c.getId())).findFirst()
                        .ifPresent(task -> {
                            if (range.contains(task.getSaleTotalAmount().intValue())) {
                                run_enbaled_list.add(x);
                            }
                        });
            });
        } else if (isCumulativeSalesRange()) {
            Preconditions.checkState(rule.getCumulativeSalesAmount().isPresent());
            Range<Integer> range = rule.getCumulativeSalesAmount().get();
            this.taskDetails.forEach(x -> {
                this.memberInfos.stream().filter(m ->
                        Objects.equal(x.getMemberId(), m.getMemberId())).findFirst().ifPresent(mm -> {
                    if (range.contains(mm.cumulativeSalesAmount.intValue())) {
                        run_enbaled_list.add(x);
                    }
                });
            });
        } else {
            run_enbaled_list.addAll(this.taskDetails);
        }
        if (CollectionUtils.isEmpty(run_enbaled_list)) return Optional.empty();
        Preconditions.checkState(rule.getAutoRunChannel().isPresent());
        AutoRunChannel autoRunChannel = rule.getAutoRunChannel().get();
        List<UpcomingTaskDetailExecDto> res = run_enbaled_list.stream()
                .map(x -> new UpcomingTaskDetailExecDto(x, autoRunChannel)).collect(Collectors.toList());
        return Optional.of(res);
    }

    public boolean isError() {
        return error;
    }

    public boolean isSingleSalesRange() {
        return rule.getSingleSalesAmount().isPresent();
    }

    public boolean isCumulativeSalesRange() {
        return rule.getCumulativeSalesAmount().isPresent();
    }

    public TaskCareRule4Touch90Entity getRule() {
        return rule;
    }

    public List<UpcomingTaskDetailEntity> getTaskDetails() {
        return taskDetails;
    }

    public Optional<List<UpcomingTaskDetailEntity>> selfTest() {
        if (rule == null) {
            List<UpcomingTaskDetailEntity> error_list = taskDetails.stream().map(x -> x.makeException("缺少规则"))
                    .collect(Collectors.toList());
            error = true;
            return Optional.of(error_list);
        } else {
            if (!rule.isAutoRun()) {
                List<UpcomingTaskDetailEntity> error_list = taskDetails.stream().map(x -> x.makeException("自动执行任务已被中止"))
                        .collect(Collectors.toList());
                error = true;
                return Optional.of(error_list);
            }
            if (!rule.getAutoRunChannel().isPresent()) {
                List<UpcomingTaskDetailEntity> error_list = taskDetails.stream().map(x -> x.makeException("缺少必要的发送渠道参数"))
                        .collect(Collectors.toList());
                error = true;
                return Optional.of(error_list);
            }
            if (!rule.getAutoRunChannel().isPresent()) {
                List<UpcomingTaskDetailEntity> error_list = taskDetails.stream().map(x -> x.makeException("缺少必要的发送渠道参数"))
                        .collect(Collectors.toList());
                error = true;
                return Optional.of(error_list);
            }
        }
        return Optional.empty();
    }

    public void addUpcomingTaskEntitys(List<UpcomingTaskEntity> upcomingTasks) {
        if (CollectionUtils.isNotEmpty(upcomingTasks))
            this.upcomingTasks.addAll(upcomingTasks);
    }

    public void addMemberInfos(List<Map<String, Object>> memberInfos) {
        memberInfos.forEach(map -> this.memberInfos.add(new MemberInfo(MapUtils.getInteger(map, "memberId"),
                MapUtils.getLong(map, "c_total_buy_amount01"))));
    }

    class MemberInfo {
        private final Integer memberId;
        private final Long cumulativeSalesAmount;

        MemberInfo(Integer memberId, Long cumulativeSalesAmount) {
            this.memberId = memberId;
            this.cumulativeSalesAmount = cumulativeSalesAmount;
        }

        Integer getMemberId() {
            return memberId;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("memberId", memberId)
                    .add("cumulativeSalesAmount", cumulativeSalesAmount)
                    .toString();
        }
    }

    public List<Integer> getTaskIdsParams() {
        return this.taskDetails.stream().map(UpcomingTaskDetailEntity::getTaskId).collect(Collectors.toList());
    }


    public Map<String, Object> getCumulativeSalesParams() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", rule.getCompanyId());
        List<Integer> memberIds = this.taskDetails.stream().map(UpcomingTaskDetailEntity::getMemberId).collect(Collectors.toList());
        params.put("memberIds", memberIds);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rule", rule == null ? "" : rule.getId())
                .add("taskDetails", taskDetails.size())
                .toString();
    }
}
