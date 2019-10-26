package com.legooframework.model.membercare.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class TaskCareRule4Touch90Entity extends TaskCareRuleEntity {

    private boolean cancelBefore;
    private Range<Integer> maxConsumptionDays;
    private Range<Integer> maxAmountOfconsumption;
    private List<TaskCareDetailRule> ruleDetails;
    // 自动发送配置
    private AutoRunChannel autoRunChannel;
    private Range<Integer> singleSalesAmount;
    private Range<Integer> cumulativeSalesAmount;
    private boolean autoRun;

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = super.toViewMap();
        if (super.isOpenMerge()) {
            params.put("maxConsumptionDays", maxConsumptionDays.upperEndpoint());
            params.put("maxAmountOfconsumption", maxAmountOfconsumption.upperEndpoint());
            params.put("cancelBefore", cancelBefore);
        }
        params.put("id", getId());
        params.put("openMerge", super.isOpenMerge());
        params.put("autoRun", this.autoRun);
        if (this.autoRun) {
            params.put("autoRunChannel", autoRunChannel.getChannel());
            if (singleSalesAmount != null) {
                params.put("singleSalesAmount", singleSalesAmount.lowerEndpoint());
            } else if (cumulativeSalesAmount != null) {
                params.put("cumulativeSalesAmount", String.format("%s:%s", cumulativeSalesAmount.lowerEndpoint(),
                        cumulativeSalesAmount.upperEndpoint()));
            }
        }
        List<Map<String, Object>> list = Lists.newArrayList();
        ruleDetails.forEach(rule -> list.add(rule.toViewMap()));
        params.put("ruleDetail", list);
        return params;
    }

    // 90规则设定
    // 天地才三位
    private TaskCareRule4Touch90Entity(String categories, String mergeBuilderSpec, String ruleBuilderSpec, String autoRunBuilderSpec,
                                       Integer companyId, Integer storeId, boolean enabled, Long userId) {
        super(companyId, storeId, BusinessType.TOUCHED90, categories, ruleBuilderSpec, enabled, userId);
        Splitter.MapSplitter MAP_SPLITTER = Splitter.on(',').trimResults().withKeyValueSeparator('=');
        Joiner.MapJoiner MAP_JOINSER = Joiner.on(',').withKeyValueSeparator('=');
        if (!Strings.isNullOrEmpty(mergeBuilderSpec)) {
            Map<String, String> params = MAP_SPLITTER.split(mergeBuilderSpec);
            Map<String, Object> _result = Maps.newHashMap();
            this.cancelBefore = MapUtils.getBoolean(params, "cancelBefore", false);
            _result.put("cancelBefore", this.cancelBefore);
            Integer maxConsumptionDays_val = MapUtils.getInteger(params, "maxConsumptionDays", null);
            Preconditions.checkNotNull(maxConsumptionDays_val, "mergeBuilderSpec=maxConsumptionDays:? 不可以为空值...");
            this.maxConsumptionDays = Range.closed(0, maxConsumptionDays_val);
            _result.put("maxConsumptionDays", MapUtils.getInteger(params, "maxConsumptionDays"));
            Integer maxAmountOfconsumption_val = MapUtils.getInteger(params, "maxAmountOfconsumption", null);
            Preconditions.checkNotNull(maxAmountOfconsumption_val, "mergeBuilderSpec=maxAmountOfconsumption:? 不可以为空值...");
            this.maxAmountOfconsumption = Range.closed(0, maxAmountOfconsumption_val);
            _result.put("maxAmountOfconsumption", MapUtils.getInteger(params, "maxAmountOfconsumption"));
            super.setMergeBuilderSpec(MAP_JOINSER.join(_result));
        } else {
            super.setMergeBuilderSpec(null);
        }
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ruleBuilderSpec), "规则明细设置项 ruleBuilderSpec  不可以空...");
        this.ruleDetails = TaskCareRuleEntityAction.parseDetail(ruleBuilderSpec);
        // 判断节点顺序
        LocalDateTime _begin = new LocalDateTime(2019, 1, 1, 0, 0, 1);
        if (this.ruleDetails.size() > 1) {
            int size = this.ruleDetails.size();
            for (int i = 1; i < size; i++) {
                TaskCareDetailRule detail = this.ruleDetails.get(i - 1);
                TaskCareDetailRule current = this.ruleDetails.get(i);
                LocalDateTime end_time = _begin.plusHours((int) (detail.getDelay().toHours() + detail.getExpired().toHours()));
                LocalDateTime start_time = _begin.plusHours((int) (current.getDelay().toHours()));
                Preconditions.checkState(end_time.isBefore(start_time), "Touch90 设置节点 %s 存在日期重叠...", detail);
            }
        }
        this.autoRun = this.ruleDetails.stream().anyMatch(TaskCareDetailRule::isAuto);
        if (this.autoRun) {
            Map<String, Object> params = parseAutoRun(autoRunBuilderSpec, MAP_SPLITTER);
            super.setAutoRunBuilderSpec(MAP_JOINSER.join(params));
        } else {
            super.setAutoRunBuilderSpec(null);
        }
    }

    /**
     * 广义相对论 与 狭义相对论 太精彩了  时间是什么  为什么他是有方向的
     * 逝者如斯夫
     *
     * @param autoRunBuilderSpec 爱因斯坦
     * @param MAP_SPLITTER       广西相对立
     * @return 色i石头
     */
    private Map<String, Object> parseAutoRun(String autoRunBuilderSpec, Splitter.MapSplitter MAP_SPLITTER) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(autoRunBuilderSpec), "自动任务发送配置不可以为空...");
        Map<String, String> params = MAP_SPLITTER.split(autoRunBuilderSpec);
        Map<String, Object> result = Maps.newHashMap();
        int channel = MapUtils.getInteger(params, "channel", -1);
        this.autoRunChannel = AutoRunChannel.parse(channel);
        result.put("channel", channel);
        Preconditions.checkArgument(channel != -1, "缺少发送渠道类型配置项 channel=?");
        int singleSalesAmount_val = MapUtils.getInteger(params, "singleSalesAmount", -1);
        String cumulativeSalesAmount_val = MapUtils.getString(params, "cumulativeSalesAmount");
        if (singleSalesAmount_val != -1) {
            this.singleSalesAmount = Range.closedOpen(singleSalesAmount_val, Integer.MAX_VALUE);
            result.put("singleSalesAmount", singleSalesAmount_val);
        } else if (!Strings.isNullOrEmpty(cumulativeSalesAmount_val)) {
            String[] range = StringUtils.split(cumulativeSalesAmount_val, ':');
            this.cumulativeSalesAmount = Range.closed(Integer.valueOf(range[0]), Integer.valueOf(range[1]));
            result.put("cumulativeSalesAmount", cumulativeSalesAmount_val);
        }
        return result;
    }

    /**
     * 创建模板规则的规则
     *
     * @param categories       波尔
     * @param mergeBuilderSpec 量子
     * @param ruleBuilderSpec  力学
     * @return 爱因斯坦 蒙了
     */
    static TaskCareRule4Touch90Entity createRuleTemplate(String categories, String mergeBuilderSpec, String ruleBuilderSpec) {
        return new TaskCareRule4Touch90Entity(categories, mergeBuilderSpec, ruleBuilderSpec, null, -1, -1, true, -1L);
    }

    /**
     * 创建 公司模式
     *
     * @param categories         规则
     * @param mergeBuilderSpec   合并
     * @param ruleBuilderSpec    规则
     * @param autoRunBuilderSpec 自动化
     * @param enabled            是否激活
     * @param user               用户
     * @return 结果
     */
    public static TaskCareRule4Touch90Entity createCompanyRule(String categories, String mergeBuilderSpec, String ruleBuilderSpec,
                                                               String autoRunBuilderSpec, boolean enabled, LoginContext user) {
        return new TaskCareRule4Touch90Entity(categories, mergeBuilderSpec, ruleBuilderSpec, autoRunBuilderSpec,
                user.getTenantId().intValue(), -1, enabled, user.getLoginId());
    }

    /**
     * 修改公司规则
     *
     * @param taskCareRule 修改后的规则实例
     * @param user         修改的当前用户信息
     * @return 了空
     */
    Optional<TaskCareRule4Touch90Entity> changeCompanyRule(TaskCareRule4Touch90Entity taskCareRule, LoginContext user) {
        if (super.equalsByRule(taskCareRule) && this.isEnabled() == taskCareRule.isEnabled()) return Optional.empty();
        // Preconditions.checkState(this.isEnabled(), "修改模式无法禁用规则 %s ....", taskCareRule.getId());
        TaskCareRule4Touch90Entity entity = TaskCareRule4Touch90Entity.createCompanyRule(taskCareRule.getCategories(),
                taskCareRule.getMergeBuilderSpec(), taskCareRule.getRuleBuilderSpec(), taskCareRule.getAutoRunBuilderSpec(),
                taskCareRule.isEnabled(), user);
        return Optional.of(entity);
    }

    Optional<Range<Integer>> getSingleSalesAmount() {
        return Optional.ofNullable(singleSalesAmount);
    }

    Optional<Range<Integer>> getCumulativeSalesAmount() {
        return Optional.ofNullable(cumulativeSalesAmount);
    }

    boolean hasAutoRunFilter() {
        return singleSalesAmount != null || cumulativeSalesAmount != null;
    }

    @Override
    public Optional<AutoRunChannel> getAutoRunChannel() {
        return Optional.ofNullable(this.autoRunChannel);
    }

    static TaskCareRule4Touch90Entity createStoreRule(TaskCareRule4Touch90Entity taskCareRule, CrmStoreEntity store,
                                                      boolean enabled, LoginContext user) {
        return new TaskCareRule4Touch90Entity(taskCareRule.getCategories(), taskCareRule.getMergeBuilderSpec(),
                taskCareRule.getRuleBuilderSpec(), taskCareRule.getAutoRunBuilderSpec(), store.getCompanyId(), store.getId(),
                enabled, user.getLoginId());
    }

    Optional<TaskCareRule4Touch90Entity> changeStoreRule(TaskCareRule4Touch90Entity taskCareRule, CrmStoreEntity store, LoginContext user) {
        if (super.equals(taskCareRule)) return Optional.empty();
        return Optional.of(TaskCareRule4Touch90Entity.createStoreRule(taskCareRule, store, taskCareRule.isEnabled(), user));
    }

    // for DB
    TaskCareRule4Touch90Entity(TaskCareRuleEntity taskCareRule) {
        super(taskCareRule);
        Splitter.MapSplitter MAP_SPLITTER = Splitter.on(',').trimResults().withKeyValueSeparator('=');
        if (taskCareRule.isOpenMerge()) {
            Map<String, String> mergeBuilderSpec = MAP_SPLITTER.split(taskCareRule.getMergeBuilderSpec());
            this.maxConsumptionDays = Range.closed(0, MapUtils.getInteger(mergeBuilderSpec, "maxConsumptionDays"));
            this.maxAmountOfconsumption = Range.closed(0, MapUtils.getInteger(mergeBuilderSpec, "maxAmountOfconsumption"));
            this.cancelBefore = MapUtils.getBoolean(mergeBuilderSpec, "cancelBefore", false);
        }
        this.ruleDetails = TaskCareRuleEntityAction.parseDetail(taskCareRule.getRuleBuilderSpec());
        this.autoRun = this.ruleDetails.stream().anyMatch(TaskCareDetailRule::isAuto);
        if (this.autoRun) parseAutoRun(taskCareRule.getAutoRunBuilderSpec(), MAP_SPLITTER);
    }

    /**
     * 禁用
     *
     * @return 方丈
     */
    Optional<TaskCareRule4Touch90Entity> disabledRule() {
        Optional<TaskCareRuleEntity> clone = super.disabled();
        return clone.map(TaskCareRule4Touch90Entity::new);
    }

    List<TaskCareDetailRule> getRuleDetails() {
        return ruleDetails;
    }

    public Optional<Touch90TaskDto> createTasks(final CrmStoreEntity store, final CrmMemberEntity member,
                                                List<UpcomingTaskEntity> touch90Tasks,
                                                List<SaleRecordEntity> sale_records) {
        sale_records.forEach(x -> Preconditions.checkState(x.getMemberId().equals(member.getId())));
        if (CollectionUtils.isNotEmpty(touch90Tasks)) {
            touch90Tasks.forEach(x -> Preconditions.checkState(x.isOwner(store, member)));
            TaskCareRuleEntityAction.sort90Tasks(touch90Tasks);
        }

        List<SaleRecordEntity> saleRecords = Lists.newArrayList(sale_records);
        Touch90TaskDto touch90TaskDto = new Touch90TaskDto(store.getCompanyId());

        // 优先处理 冲单的记录 自动合并 并取消掉
        if (saleRecords.size() > 1) {
            List<SaleRecordEntity> negative_sale = saleRecords.stream().filter(SaleRecordEntity::isNegativeOrZore)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(negative_sale)) {
                saleRecords.removeAll(negative_sale);
                List<Long> saleTotalAmounts = negative_sale.stream().map(x -> Math.abs(x.getSaleTotalAmount().longValue()))
                        .collect(Collectors.toList());
                negative_sale.clear();
                saleRecords.forEach(x -> {
                    if (saleTotalAmounts.contains(x.getSaleTotalAmount().longValue())) {
                        negative_sale.add(x);
                    }
                });
                if (CollectionUtils.isNotEmpty(negative_sale))
                    saleRecords.removeAll(negative_sale);
            }
        }

        if (CollectionUtils.isEmpty(saleRecords)) return Optional.empty();

        // 是否开启合并项
        if (!isOpenMerge()) {
            saleRecords.forEach(x -> {
                UpcomingTaskEntity _current = UpcomingTaskEntity.createJobInstance(this, member, store, x);
                _current.setTaskDetails(new UpcomingTaskDetails(createDetail(_current, x)));
                touch90TaskDto.setInserts(_current);
            });
            return Optional.of(touch90TaskDto);
        }

        UpcomingTaskEntity _current = null;
        List<SaleRecordEntity> _saleRecords = Lists.newArrayList(saleRecords);
        if (CollectionUtils.isNotEmpty(touch90Tasks)) {
            _current = touch90Tasks.get(touch90Tasks.size() - 1);
        } else {
            SaleRecordEntity _temp = _saleRecords.remove(0);
            _current = UpcomingTaskEntity.createJobInstance(this, member, store, _temp);
            _current.setTaskDetails(new UpcomingTaskDetails(createDetail(_current, _temp)));
            touch90TaskDto.setInserts(_current);
        }

        if (CollectionUtils.isEmpty(_saleRecords)) return Optional.of(touch90TaskDto);

        UpcomingTaskEntity _next;
        for (SaleRecordEntity $it : _saleRecords) {
            _next = UpcomingTaskEntity.createJobInstance(this, member, store, $it);
            _next.setTaskDetails(new UpcomingTaskDetails(createDetail(_next, $it)));
            int _days = Days.daysBetween(_current.getSaleDate(), _next.getSaleDate()).getDays();
            if (!maxConsumptionDays.contains(_days)) {
                _current = _next;
                touch90TaskDto.setInserts(_current);
                continue;
            }
            int saleTotalAmount = $it.getSaleTotalAmount().intValue();
            if (!maxAmountOfconsumption.contains(saleTotalAmount)) {
                if (cancelBefore) {
                    _current.canceled().ifPresent(x -> touch90TaskDto.setUpdateDetail(x.getTaskDetails()));
                }
                _current = _next;
                touch90TaskDto.setInserts(_current);
                continue;
            }
            _current.addMergeInfo($it.getId()).ifPresent(touch90TaskDto::setUpdates);
        }
        return Optional.of(touch90TaskDto);
    }

    private List<UpcomingTaskDetailEntity> createDetail(final UpcomingTaskEntity task, final SaleRecordEntity saleRecord) {
        final List<UpcomingTaskDetailEntity> list = Lists.newArrayList();
        this.ruleDetails.forEach(rule -> rule.createDetail(this, task, saleRecord).ifPresent(list::add));
        return list;
    }

    Set<String> getRuleIds() {
        return this.ruleDetails.stream().map(TaskCareDetailRule::getId).collect(Collectors.toSet());
    }

    public boolean isAutoRun() {
        return autoRun;
    }
//    @Override
//    public Map<String, Object> toParamMap(String... excludes) {
//        Map<String, Object> res = super.toParamMap("maxConsumptionDays", "maxAmountOfconsumption", "concalBefore", "ruleDetails");
//        String content = format("maxConsumptionDays=%s,maxAmountOfconsumption=%s,concalBefore=%s", maxConsumptionDays.upperEndpoint(),
//                maxAmountOfconsumption.upperEndpoint(), concalBefore);
//        res.put("content", content);
//        res.put("details", TaskCareRuleEntityAction.joinDetails(this.ruleDetails));
//        return res;
//    }

    @Override
    public String getCategories() {
        return super.getCategories();
    }

}
