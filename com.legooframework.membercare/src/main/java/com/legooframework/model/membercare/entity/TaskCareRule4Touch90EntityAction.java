package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.messaging.support.MessageBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class TaskCareRule4Touch90EntityAction extends BaseEntityAction<TaskCareRule4Touch90Entity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskCareRule4Touch90EntityAction.class);

    public TaskCareRule4Touch90EntityAction(TaskCareRuleEntityAction taskCareRuleEntityAction, MessagingTemplate messagingTemplate) {
        super("CrmJobsCache");
        this.taskCareRuleEntityAction = taskCareRuleEntityAction;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 返回可用的90节点列表
     *
     * @return 瑞兹科教
     */
    public Optional<List<TaskCareRule4Touch90Entity>> loadEnabledTouch90RuleTemplates() {
        return loadTouch90Rules(-1);
    }

    public Optional<TaskCareRule4Touch90Entity> findTouch90RuleTemplates(String categories) {
        Optional<List<TaskCareRule4Touch90Entity>> list = loadTouch90Rules(-1);
        return list.flatMap(o -> o.stream().filter(x -> x.isCategories(categories)).findFirst());
    }

    /**
     * 加载通用业务类型
     *
     * @param categories 狭义相对论
     * @return TaskCareRule4Touch90Entity 爱因斯坦
     */
    public TaskCareRule4Touch90Entity loadTouch90RuleTemplates(String categories) {
        Optional<List<TaskCareRule4Touch90Entity>> list = loadTouch90Rules(-1);
        Optional<TaskCareRule4Touch90Entity> opt =
                list.flatMap(o -> o.stream().filter(x -> x.isCategories(categories)).findFirst());
        Preconditions.checkState(opt.isPresent(), "缺少type=%s 对应的通用规则定义...", categories);
        return opt.get();
    }

    /**
     * 新增或则修改90 通用模板
     *
     * @param categories      风萧萧 额若肚子  钱钟书
     * @param ruleBuilderSpec 壮士已取消 写在人生边上 苦啊
     */
    public void saveOrUpdateTouch90RuleTemplate(String categories, String mergeBuilderSpec, String ruleBuilderSpec) {
        TaskCareRule4Touch90Entity entity = TaskCareRule4Touch90Entity.createRuleTemplate(categories, mergeBuilderSpec, ruleBuilderSpec);
        Optional<List<TaskCareRule4Touch90Entity>> list = loadTouch90Rules(-1);
        if (list.isPresent()) {
            Optional<TaskCareRule4Touch90Entity> opt_entity = list.get().stream().filter(x -> x.isCategories(categories)).findFirst();
            if (opt_entity.isPresent() && opt_entity.get().equals(entity)) return;
            taskCareRuleEntityAction.insertBeforeDelete(entity);
        } else {
            taskCareRuleEntityAction.insertBeforeDelete(entity);
        }
        evictCache(-1);
    }

    private void evictCache(Integer companyId) {
        getCache().ifPresent(c -> c.evict(String.format("%s_%s_%s", getModelName(), BusinessType.TOUCHED90.getJobName(), companyId)));
    }

    /**
     * @param id 解惑指定ID的任务规则
     * @return 任务自身的一些信息
     */
    @Override
    public Optional<TaskCareRule4Touch90Entity> findById(Object id) {
        Preconditions.checkNotNull(id, "待查询的ID 不可以为空值...");
        String str_id = (String) id; // 100098_-1_TOUCHED90_0
        int companyId = Integer.valueOf(StringUtils.split(str_id, '_')[0]);
        Optional<List<TaskCareRule4Touch90Entity>> company_all_list = loadTouch90Rules(companyId);
        return company_all_list.flatMap(rule -> rule.stream().filter(x -> StringUtils.equals(x.getId(), str_id)).findFirst());
    }

    /**
     * 查理芒格 只剩大点
     * 采用 事件驱动架构 不再输出返回值
     *
     * @param careRule       貌美如花 历史天空
     * @param user           天涯浪客 我想回家
     * @param stores         百转千回 沃夫慈悲
     * @param incloudCompany 是否包含公司
     */
    public synchronized void addTouch90Rule(TaskCareRule4Touch90Entity careRule, boolean incloudCompany,
                                            Collection<CrmStoreEntity> stores, LoginContext user) {
        if (logger.isDebugEnabled())
            logger.debug("addTouch90Rule(%s,incloudCompany：%s，companyId = %s) start...", careRule, incloudCompany, user.getTenantId());
        List<Touch90RuleDifference> differences = Lists.newArrayList();
        List<TaskCareRule4Touch90Entity> insert_care_list = Lists.newArrayList();
        List<CrmStoreEntity> insert_list_stores = Lists.newArrayList();
        List<RuleWithStore> update_list_rule = Lists.newArrayList();
        // 创建公司规则
        final TaskCareRule4Touch90Entity company_care_rule = TaskCareRule4Touch90Entity.createCompanyRule(careRule.getCategories(),
                careRule.getMergeBuilderSpec(), careRule.getRuleBuilderSpec(), careRule.getAutoRunBuilderSpec(), true, user);

        Optional<List<TaskCareRule4Touch90Entity>> company_all_list = loadTouch90Rules(user.getTenantId().intValue());
        boolean has_company_all_list = company_all_list.isPresent();
        // 如果包含公司则进行公司处理
        if (incloudCompany) {
            if (has_company_all_list) {
                Optional<TaskCareRule4Touch90Entity> exits_opt = company_all_list.get().stream()
                        .filter(TaskCareRuleEntity::isCompany)
                        .filter(x -> x.isCategories(company_care_rule.getCategories())).findFirst();
                Preconditions.checkState(!exits_opt.isPresent(), "当前公司已经存在type = %s 对应的规则定义...", careRule.getCategories());
                insert_care_list.add(company_care_rule);
            } else {
                insert_care_list.add(company_care_rule);
            }
            if (logger.isDebugEnabled())
                logger.debug(String.format("addTouch90Rule(....) 新增公司规则 %s", company_care_rule));
        }

        // 是否含有下级门店操作
        if (CollectionUtils.isNotEmpty(stores)) {
            if (has_company_all_list) {
                List<TaskCareRule4Touch90Entity> exits_store_rule_list = company_all_list.get().stream()
                        .filter(TaskCareRuleEntity::isStore)
                        .filter(x -> x.isCategories(company_care_rule.getCategories()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(exits_store_rule_list)) {
                    // 公司无此类型 门店记录
                    insert_list_stores.addAll(stores);
                } else {
                    stores.forEach(store -> {
                        Optional<TaskCareRule4Touch90Entity> opt = exits_store_rule_list.stream().filter(x -> x.isStore(store))
                                .findFirst();
                        if (opt.isPresent()) {
                            update_list_rule.add(createRuleWithStore4Insert(store, opt.get()));
                        } else {
                            insert_list_stores.add(store);
                        }
                    });
                }
            } else {
                insert_list_stores.addAll(stores);
            }
        }
        if (CollectionUtils.isNotEmpty(insert_care_list)) taskCareRuleEntityAction.batchInsert(insert_care_list);
        batchInsertByStores(company_care_rule, insert_list_stores, user);
        batchUpdateByStores(company_care_rule, update_list_rule, user).ifPresent(differences::addAll);

        evictCache(user.getTenantId().intValue());

        publishDifferenceEvent(differences, user);
        if (logger.isDebugEnabled())
            logger.debug(String.format("addTouch90Rule(%s,companyId = %s) ADD %s finished and clear cache", careRule.getCategories(),
                    user.getTenantId(), company_care_rule));

        publishRuleEvent(careRule, incloudCompany, insert_list_stores, update_list_rule, user);
    }

    /**
     * @param user           小飞象
     * @param incloudCompany 是否包含公司
     * @param stores         你的样子
     * @param careRule       我要飞扬
     */
    public synchronized void updateTouch90Rule(boolean incloudCompany, Collection<CrmStoreEntity> stores,
                                               TaskCareRule4Touch90Entity careRule, LoginContext user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateTouch90Rule(incloudCompany:%s,companyId = %s,careRule= %s) start...",
                    incloudCompany, user.getTenantId(), careRule));
        Preconditions.checkNotNull(careRule, "待更新的规则 不可为空值 ...");
        List<Touch90RuleDifference> differences = Lists.newArrayList();
        List<TaskCareRule4Touch90Entity> update_rule_list = Lists.newArrayList();
        List<CrmStoreEntity> insert_list_stores = Lists.newArrayList();
        List<RuleWithStore> update_list_rule = Lists.newArrayList();
        //  处理公司相关事务
        Optional<List<TaskCareRule4Touch90Entity>> company_all_rules = loadTouch90Rules(user.getTenantId().intValue());

        if (incloudCompany) {
            // 处理含有公司的信息
            Preconditions.checkState(company_all_rules.isPresent(), "该公司尚未配置任何90 规则...");
            Optional<TaskCareRule4Touch90Entity> com_only_rule = company_all_rules.get().stream()
                    .filter(TaskCareRuleEntity::isCompany)
                    .filter(x -> x.isCategories(careRule.getCategories()))
                    .findFirst();
            Preconditions.checkState(com_only_rule.isPresent(), "公司%s对应类型为%s的90规则不存在...", user.getTenantId(),
                    careRule.getCategories());
            TaskCareRule4Touch90Entity com_temp_rule = TaskCareRule4Touch90Entity.createCompanyRule(careRule.getCategories(),
                    careRule.getMergeBuilderSpec(), careRule.getRuleBuilderSpec(), careRule.getAutoRunBuilderSpec(), true, user);
            com_only_rule.get().changeCompanyRule(com_temp_rule, user).ifPresent(rule -> {
                update_rule_list.add(rule);
                diff(com_only_rule.get(), rule).ifPresent(differences::addAll);
            });
        }

        // 是否含有下级门店
        if (CollectionUtils.isNotEmpty(stores)) {
            if (company_all_rules.isPresent()) {
                List<TaskCareRule4Touch90Entity> exits_store_rules = company_all_rules.get().stream()
                        .filter(TaskCareRuleEntity::isStore)
                        .filter(x -> x.isCategories(careRule.getCategories()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(exits_store_rules)) {
                    // 公司无此类型 门店记录
                    insert_list_stores.addAll(stores);
                } else {
                    stores.forEach(store -> {
                        Optional<TaskCareRule4Touch90Entity> opt = exits_store_rules.stream().filter(x -> x.isStore(store))
                                .findFirst();
                        if (opt.isPresent()) {
                            update_list_rule.add(createRuleWithStore4Update(store, opt.get()));
                        } else {
                            insert_list_stores.add(store);
                        }
                    });
                }
            } else {
                insert_list_stores.addAll(stores);
            }
        }

        if (CollectionUtils.isNotEmpty(update_rule_list)) {
            taskCareRuleEntityAction.batchDisabledByIds(update_rule_list);
            taskCareRuleEntityAction.batchInsert(update_rule_list);
            if (logger.isDebugEnabled())
                logger.debug("updateTouch90Rule(...) 修改公司配置项: %s", update_rule_list);
        }
        batchInsertByStores(careRule, insert_list_stores, user);
        batchUpdateByStores(careRule, update_list_rule, user).ifPresent(differences::addAll);
        publishDifferenceEvent(differences, user);

        evictCache(user.getTenantId().intValue());

        if (logger.isDebugEnabled())
            logger.debug("updateTouch90Rule(companyId = %s) update %s finished and clear cache", user.getTenantId(),
                    update_rule_list);

        publishRuleEvent(careRule, incloudCompany, insert_list_stores, update_list_rule, user);
    }

    /**
     * 删除指定类型的90节点
     *
     * @param categories     节点
     * @param user           姐姐
     * @param incloudCompany 妹妹
     * @param stores         纷纷
     */
    public synchronized void removeTouch90Rule(String categories, boolean incloudCompany, Collection<CrmStoreEntity> stores,
                                               LoginContext user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("removeTouch90Rule(categories:%s,incloudCompany= %s,companyId = %s) start...",
                    categories, incloudCompany, user.getTenantId()));
        List<Touch90RuleDifference> differences = Lists.newArrayList();
        List<TaskCareRule4Touch90Entity> update_rule_list = Lists.newArrayList();

        Optional<List<TaskCareRule4Touch90Entity>> company_all_list = loadTouch90Rules(user.getTenantId().intValue());
        if (incloudCompany) {
            Preconditions.checkState(company_all_list.isPresent(), "该公司尚未配置任何90 规则...");
            Optional<TaskCareRule4Touch90Entity> optional = company_all_list.get().stream().filter(TaskCareRuleEntity::isCompany)
                    .filter(x -> x.isCategories(categories)).findFirst();
            Preconditions.checkState(optional.isPresent(), "公司%s不存在对应的90规则...", user.getTenantId());
            update_rule_list.add(optional.get());
            differences.add(Touch90RuleDifference.ruleRemove(optional.get()));
        }

        if (CollectionUtils.isNotEmpty(stores) && company_all_list.isPresent()) {
            List<TaskCareRule4Touch90Entity> store_exits_rules = company_all_list.get().stream().filter(TaskCareRuleEntity::isStore)
                    .filter(x -> x.isCategories(categories)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(store_exits_rules)) {
                store_exits_rules.forEach(rule -> {
                    update_rule_list.add(rule);
                    differences.add(Touch90RuleDifference.ruleRemove(rule));
                });
            }
        }

        if (CollectionUtils.isNotEmpty(update_rule_list)) {
            taskCareRuleEntityAction.batchDisabledByIds(update_rule_list);
            if (logger.isDebugEnabled())
                logger.debug(String.format("removeTouch90Rule(%s.incloudCompany:%s) 90规则配置项: %s", categories, incloudCompany,
                        update_rule_list));
        }

        evictCache(user.getTenantId().intValue());

        publishDifferenceEvent(differences, user);
    }

    /**
     * 禁用就是一个混搭  画派情缘
     *
     * @param categories 赐福 聚少离多
     * @param stores     送行 历史长河
     * @param user       别离 奔流不止
     */
    public synchronized void disabledTouch90Rule(String categories, boolean incloudCompany, Collection<CrmStoreEntity> stores,
                                                 LoginContext user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("disabledTouch90Rule(categories = %s,stores=%s) start", categories,
                    CollectionUtils.isEmpty(stores) ? 0 : stores.size()));
        List<TaskCareRule4Touch90Entity> update_list = Lists.newArrayList();
        List<TaskCareRule4Touch90Entity> insert_list = Lists.newArrayList();
        List<Touch90RuleDifference> differences = Lists.newArrayList();
        TaskCareRule4Touch90Entity rule_template = loadTouch90RuleTemplates(categories);
        Optional<List<TaskCareRule4Touch90Entity>> company_all_list = loadTouch90Rules(user.getTenantId().intValue());
        if (incloudCompany && company_all_list.isPresent()) {
            Optional<TaskCareRule4Touch90Entity> com_categories_rule = company_all_list.get().stream()
                    .filter(TaskCareRule4Touch90Entity::isCompany).filter(x -> x.isCategories(categories)).findFirst();
            Preconditions.checkState(com_categories_rule.isPresent(), "公司对应的规则不存在");
            Preconditions.checkState(com_categories_rule.get().isEnabled(), "公司对应的规则目前处于无效状态");
            com_categories_rule.get().disabledRule().ifPresent(update_list::add);
            rule_template = update_list.get(0);
            differences.add(Touch90RuleDifference.ruleDisabled(rule_template));
        }

        final TaskCareRule4Touch90Entity categories_rule_template = rule_template;

        if (CollectionUtils.isNotEmpty(stores) && company_all_list.isPresent()) {
            stores.forEach(store -> {
                Optional<TaskCareRule4Touch90Entity> optional_store_rule = company_all_list.get().stream()
                        .filter(x -> x.isStore(store))
                        .filter(TaskCareRuleEntity::isEnabled)
                        .filter(x -> x.isCategories(categories))
                        .findFirst();
                if (optional_store_rule.isPresent()) {
                    optional_store_rule.get().disabledRule().ifPresent(x -> {
                        update_list.add(x);
                        differences.add(Touch90RuleDifference.ruleDisabled(x));
                    });
                } else {
                    insert_list.add(TaskCareRule4Touch90Entity.createStoreRule(categories_rule_template, store, false, user));
                }
            });
        }

        if (CollectionUtils.isNotEmpty(insert_list)) taskCareRuleEntityAction.batchInsert(insert_list);

        if (CollectionUtils.isNotEmpty(update_list)) {
            List<String> ids = update_list.stream().map(BaseEntity::getId).collect(Collectors.toList());
            taskCareRuleEntityAction.disabledByIds(ids);
        }

        if (logger.isDebugEnabled())
            logger.debug(String.format("disabledTouch90Rule() update size is %s and insert size is %s", update_list.size(),
                    insert_list.size()));

        evictCache(user.getTenantId().intValue());

        publishDifferenceEvent(differences, user);
    }

    private RuleWithStore createRuleWithStore4Insert(CrmStoreEntity store, TaskCareRule4Touch90Entity careRule) {
        return new RuleWithStore(store, careRule, "insert");
    }

    private RuleWithStore createRuleWithStore4Update(CrmStoreEntity store, TaskCareRule4Touch90Entity careRule) {
        return new RuleWithStore(store, careRule, "update");
    }

    private RuleWithStore createRuleWithStore4Disabled(CrmStoreEntity store, TaskCareRule4Touch90Entity careRule) {
        return new RuleWithStore(store, careRule, "disabled");
    }

    private RuleWithStore createRuleWithStore4Enabled(CrmStoreEntity store, TaskCareRule4Touch90Entity careRule) {
        return new RuleWithStore(store, careRule, "enabled");
    }

    class RuleWithStore {

        private final CrmStoreEntity store;
        private final TaskCareRule4Touch90Entity careRule;
        private final String action;

        RuleWithStore(CrmStoreEntity store, TaskCareRule4Touch90Entity careRule, String action) {
            this.store = store;
            this.careRule = careRule;
            this.action = action;
        }

        boolean isInsert() {
            return StringUtils.equals("insert", this.action);
        }

        boolean isUpdate() {
            return StringUtils.equals("update", this.action);
        }

        boolean isEnabled() {
            return StringUtils.equals("enabled", this.action);
        }

        boolean isDisabled() {
            return StringUtils.equals("disabled", this.action);
        }

        CrmStoreEntity getStore() {
            return store;
        }

        TaskCareRule4Touch90Entity getCareRule() {
            return careRule;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("store", store)
                    .add("action", action)
                    .add("careRule", careRule)
                    .toString();
        }
    }

    /**
     * 批量新增一批门店规则 内部使用
     *
     * @param source_care_rule 蝴蝶
     * @param stores           菲菲
     * @param user             你的样子
     */
    private void batchInsertByStores(TaskCareRule4Touch90Entity source_care_rule, Collection<CrmStoreEntity> stores,
                                     LoginContext user) {
        if (CollectionUtils.isNotEmpty(stores)) {
            List<TaskCareRule4Touch90Entity> save_store_rule_list = stores.stream()
                    .map(store -> TaskCareRule4Touch90Entity.createStoreRule(source_care_rule, store, true, user))
                    .collect(Collectors.toList());
            taskCareRuleEntityAction.batchInsert(save_store_rule_list);
            if (logger.isDebugEnabled())
                logger.debug(String.format("当前无下级门店规则记录，批量新增记录到门店..共计数量：%s", stores.size()));
            evictCache(user.getTenantId().intValue());
        }
    }

    private Optional<List<Touch90RuleDifference>> batchUpdateByStores(TaskCareRule4Touch90Entity source_care_rule,
                                                                      List<RuleWithStore> ruleWithStores,
                                                                      LoginContext user) {
        List<Touch90RuleDifference> differences = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(ruleWithStores)) {
            List<TaskCareRule4Touch90Entity> save_store_rules = Lists.newArrayList();
            ruleWithStores.forEach(ruleWithStore -> {
                TaskCareRule4Touch90Entity store_rule = TaskCareRule4Touch90Entity
                        .createStoreRule(source_care_rule, ruleWithStore.getStore(), source_care_rule.isEnabled(), user);
                diff(store_rule, ruleWithStore.getCareRule()).ifPresent(differences::addAll);
                ruleWithStore.getCareRule().changeStoreRule(store_rule, ruleWithStore.getStore(), user)
                        .ifPresent(save_store_rules::add);
                evictCache(user.getTenantId().intValue());
            });
            if (CollectionUtils.isNotEmpty(save_store_rules)) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("batchUpdateByStores(...) value is %s", save_store_rules));
                taskCareRuleEntityAction.batchDisabledByIds(save_store_rules);
                taskCareRuleEntityAction.batchInsert(save_store_rules);
                evictCache(user.getTenantId().intValue());
            }
        }
        return Optional.ofNullable(CollectionUtils.isEmpty(differences) ? null : differences);
    }

    /**
     * 获取差异化结果
     *
     * @param sourceRule 公司标准
     * @param targetRule 待比较规则
     * @return 差异化结果
     */
    private Optional<List<Touch90RuleDifference>> diff(TaskCareRule4Touch90Entity sourceRule,
                                                       TaskCareRule4Touch90Entity targetRule) {
        List<Touch90RuleDifference> differences = Lists.newArrayList();
        // REMOVE NODE
        targetRule.getRuleDetails().forEach(rule -> {
            Optional<TaskCareDetailRule> exit_opt = sourceRule.getRuleDetails().stream()
                    .filter(source -> StringUtils.equals(rule.getId(), source.getId())).findFirst();
            if (!exit_opt.isPresent()) {
                differences.add(Touch90RuleDifference.nodeRemoves(targetRule, rule.getId()));
            }
        });

        // ADD NODE
        sourceRule.getRuleDetails().forEach(rule -> {
            Optional<TaskCareDetailRule> exit_opt = targetRule.getRuleDetails().stream()
                    .filter(target -> StringUtils.equals(rule.getId(), target.getId())).findFirst();
            if (!exit_opt.isPresent()) {
                differences.add(Touch90RuleDifference.nodeAdd(targetRule, rule.getId()));
            }
        });

        // 其他变化情况
        sourceRule.getRuleDetails().forEach(rule -> {
            Optional<TaskCareDetailRule> exit_opt = targetRule.getRuleDetails().stream()
                    .filter(target -> StringUtils.equals(rule.getId(), target.getId())).findFirst();
            if (exit_opt.isPresent()) {
                if (exit_opt.get().isDisabled(rule)) {
                    // 禁用节点
                    differences.add(Touch90RuleDifference.nodeDisabled(targetRule, exit_opt.get().getId()));
                } else if (exit_opt.get().isEnabled(rule)) {
                    // 启用节点
                    differences.add(Touch90RuleDifference.nodeEnabled(targetRule, exit_opt.get().getId()));
                } else if (exit_opt.get().isCloseAuto(rule)) {
                    // 关闭自动发送
                    differences.add(Touch90RuleDifference.nodeCloseAuto(targetRule, exit_opt.get().getId()));
                } else if (exit_opt.get().isOpenAuto(rule)) {
                    // 开启自动发送
                    differences.add(Touch90RuleDifference.nodeOpenAuto(targetRule, exit_opt.get().getId(),
                            exit_opt.get().getStartTime()));
                } else if (exit_opt.get().isChangeTime(rule)) {
                    // 修改发送时间
                    differences.add(Touch90RuleDifference.nodeChangeTime(targetRule, exit_opt.get().getId(),
                            exit_opt.get().getStartTime()));
                }
            }
        });
        return Optional.ofNullable(CollectionUtils.isEmpty(differences) ? null : differences);
    }

    /**
     * @param differences 我的差异化
     * @param user        我的我的
     */
    private void publishDifferenceEvent(List<Touch90RuleDifference> differences, LoginContext user) {
        if (CollectionUtils.isNotEmpty(differences)) {
            if (logger.isDebugEnabled())
                logger.debug(String.format(" symmetric_differences_com : %s", differences));
            messagingTemplate.send(TaskCareRuleConstant.CHANNEL_MEMBERCARE_INNSER_BUSEVENT, MessageBuilder.withPayload(differences)
                    .setHeader("user", user).setHeader("action", TaskCareRuleConstant.ACTION_TOUCH90RULEDIFFERENCE).build());
        }
    }

    /**
     * 捕获规则本身的一些变化情况
     *
     * @param careRule         规则自身嫩
     * @param incloudCompany   是否含公司
     * @param stores           所属门店
     * @param update_list_rule 修改历史
     * @param user             当前登陆用户信息
     */
    private void publishRuleEvent(TaskCareRule4Touch90Entity careRule, boolean incloudCompany, Collection<CrmStoreEntity> stores,
                                  List<RuleWithStore> update_list_rule, LoginContext user) {
        Set<CrmStoreEntity> store_list = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(stores)) store_list.addAll(stores);
        if (CollectionUtils.isNotEmpty(update_list_rule)) {
            // 发送的变动 需要进一步明确变动的情况
            update_list_rule.forEach(_temp -> store_list.add(_temp.getStore()));
        }
        if (!incloudCompany && CollectionUtils.isEmpty(store_list)) return;
        messagingTemplate.send(TaskCareRuleConstant.CHANNEL_MEMBERCARE_INNSER_BUSEVENT,
                MessageBuilder.withPayload(Touch90RuleDifference.ruleAdd(careRule, incloudCompany, store_list))
                        .setHeader("user", user).setHeader("action", TaskCareRuleConstant.ACTION_TOUCH90RULE_ADD).build());
    }

    /**
     * 获取门店指定的touch90 规则
     *
     * @param store      在水乙方
     * @param categories 说文解字
     * @return 国学
     */
    public synchronized TaskCareRule4Touch90Entity loadRuleByStoreWithCategories(CrmStoreEntity store, final String categories) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadRuleByStoreWithCategories(%s,%s) start", store.getId(), categories));
        Preconditions.checkNotNull(store, "所属门店不可以为空值...");
        Optional<TaskCareRule4Touch90Entity> optional = findTouch90RuleByStore(store, categories);
        Preconditions.checkState(optional.isPresent(), "门店%s 对应的%s 类型不存在", store.getId(), categories);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadRuleByStoreWithCategories(%s,%s) res is %s", store.getId(), categories, optional.get()));
        return optional.get();
    }

    private Optional<TaskCareRule4Touch90Entity> findTouch90RuleByStore(CrmStoreEntity store, final String categories) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findTouch90RuleByStore(%s,%s) start", store.getId(), categories));
        Preconditions.checkNotNull(store, "所属门店不可以为空值...");
        Optional<List<TaskCareRule4Touch90Entity>> touch90_list = loadTouch90Rules(store.getCompanyId());
        if (!touch90_list.isPresent()) return Optional.empty();
        Optional<TaskCareRule4Touch90Entity> store_rule_opt = touch90_list.get().stream()
                .filter(x -> x.isCategories(categories))
                .filter(x -> x.isStore(store))
                .findFirst();
        if (store_rule_opt.isPresent()) return store_rule_opt;
        return touch90_list.get().stream()
                .filter(TaskCareRule4Touch90Entity::isCompany)
                .filter(x -> x.isCategories(categories))
                .findFirst();
    }

    /**
     * 加载门店可用的 rule
     *
     * @param store 说文解字
     * @return 民国国粹
     */
    private synchronized Optional<List<TaskCareRule4Touch90Entity>> loadTouch90RuleByStore(CrmStoreEntity store) {
        Preconditions.checkNotNull(store, "入参门店不可以为空值...");
        Optional<List<TaskCareRule4Touch90Entity>> touch90_list = loadTouch90Rules(store.getCompanyId());
        if (!touch90_list.isPresent()) return Optional.empty();
        List<TaskCareRule4Touch90Entity> com_enabled_list = touch90_list.get().stream().filter(TaskCareRuleEntity::isCompany)
                .filter(TaskCareRuleEntity::isEnabled).collect(Collectors.toList());
        List<TaskCareRule4Touch90Entity> store_enabled_list = touch90_list.get().stream().filter(x -> x.isStore(store))
                .filter(TaskCareRuleEntity::isEnabled).collect(Collectors.toList());
        List<TaskCareRule4Touch90Entity> store_disabled_list = touch90_list.get().stream().filter(x -> x.isStore(store))
                .filter(x -> !x.isEnabled()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(store_enabled_list)) return Optional.of(store_enabled_list);
        if (CollectionUtils.isNotEmpty(com_enabled_list)) {
            List<String> disabled_categories = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(store_disabled_list)) {
                store_disabled_list.forEach(x -> disabled_categories.add(x.getCategories()));
            }
            if (CollectionUtils.isEmpty(disabled_categories)) return Optional.of(com_enabled_list);
            // 过滤禁止的规则
            com_enabled_list = com_enabled_list.stream().filter(x -> !disabled_categories.contains(x.getCategories()))
                    .collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(com_enabled_list) ? null : com_enabled_list);
        }
        return Optional.empty();
    }

    /**
     * 加载指定门店 可用的90 规则
     *
     * @param store 门店名称-东流水 先后到带氨水
     * @return 你的上帝-阿门
     */
    public Optional<List<TaskCareRule4Touch90Entity>> loadAllTouch90RuleByStore(CrmStoreEntity store, boolean onlyStore) {
        Optional<List<TaskCareRule4Touch90Entity>> store_list = loadTouch90Rules(store.getCompanyId());
        if (!store_list.isPresent()) return Optional.empty();
        List<TaskCareRule4Touch90Entity> res_list = store_list.get().stream().filter(x -> x.isStore(store))
                .collect(Collectors.toList());
        if (onlyStore) {
            res_list = res_list.stream().filter(TaskCareRuleEntity::isStore)
                    .collect(Collectors.toList());
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllTouch90RuleByStore(%s,onlyStore:%s) res is -> %s", store.getId(),
                    onlyStore, res_list));
        return Optional.ofNullable(CollectionUtils.isEmpty(res_list) ? null : res_list);
    }

    /**
     * 加载指定门店 可用的90 规则
     *
     * @param store 门店名称-东流水
     * @return 你的上帝-阿门
     */
    public synchronized Optional<List<TaskCareRule4Touch90Entity>> loadEnabledTouch90RuleByStore(CrmStoreEntity store,
                                                                                                 boolean onlyStore) {
        Optional<List<TaskCareRule4Touch90Entity>> store_list = loadTouch90RuleByStore(store);
        if (!store_list.isPresent()) return Optional.empty();
        List<TaskCareRule4Touch90Entity> res_list = store_list.get().stream().filter(TaskCareRuleEntity::isEnabled)
                .collect(Collectors.toList());
        if (onlyStore && CollectionUtils.isNotEmpty(res_list)) {
            res_list = res_list.stream().filter(TaskCareRuleEntity::isStore).collect(Collectors.toList());
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEnabledTouch90RuleByStore(%s) res is -> %s", store.getId(), res_list));
        return Optional.ofNullable(CollectionUtils.isEmpty(res_list) ? null : res_list);
    }

    public Optional<List<TaskCareRule4Touch90Entity>> loadAllTouch90RuleByCompany(CrmOrganizationEntity company) {
        Optional<List<TaskCareRule4Touch90Entity>> touch90CareRules = loadTouch90Rules(company.getId());
        if (!touch90CareRules.isPresent()) return Optional.empty();
        List<TaskCareRule4Touch90Entity> touch90_rules = touch90CareRules.get().stream()
                .filter(TaskCareRuleEntity::isCompany).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(touch90_rules) ? null : touch90_rules);
    }

    public Optional<Set<String>> loadAll90Categories(CrmOrganizationEntity company) {
        Set<String> categories = null;
        Optional<List<TaskCareRule4Touch90Entity>> touch90CareRules = loadTouch90Rules(company.getId());
        if (!touch90CareRules.isPresent()) return Optional.empty();
        categories = touch90CareRules.get().stream().map(TaskCareRule4Touch90Entity::getCategories).collect(Collectors.toSet());
        return Optional.ofNullable(CollectionUtils.isEmpty(categories) ? null : categories);
    }

    @SuppressWarnings("unchecked")
    private Optional<List<TaskCareRule4Touch90Entity>> loadTouch90Rules(final Integer companyId) {
        final String cache_key = String.format("%s_%s_%s", getModelName(), BusinessType.TOUCHED90.getJobName(), companyId);
        if (getCache().isPresent()) {
            List<TaskCareRule4Touch90Entity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        Optional<List<TaskCareRuleEntity>> taskCareRules = taskCareRuleEntityAction.loadTaskCareRules(companyId, BusinessType.TOUCHED90);
        if (!taskCareRules.isPresent()) return Optional.empty();
        List<TaskCareRule4Touch90Entity> touch90CareRules = taskCareRules.get().stream().map(TaskCareRule4Touch90Entity::new)
                .collect(Collectors.toList());
        getCache().ifPresent(c -> c.put(cache_key, touch90CareRules));
        return Optional.of(touch90CareRules);
    }

    @Override
    protected RowMapper<TaskCareRule4Touch90Entity> getRowMapper() {
        return null;
    }

    private TaskCareRuleEntityAction taskCareRuleEntityAction;
    private MessagingTemplate messagingTemplate;

}
