package com.legooframework.model.autotask.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskRuleEntityAction extends BaseEntityAction<TaskRuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskSourceEntityAction.class);

    public TaskRuleEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public void batchAddRule(Collection<TaskRuleEntity> add_rules) {
        if (CollectionUtils.isEmpty(add_rules)) return;
        Optional<List<TaskRuleEntity>> all_rules = findAll();
        List<TaskRuleEntity> addList = Lists.newArrayList(add_rules);
        if (all_rules.isPresent()) {
            for (TaskRuleEntity add_rule : add_rules) {
                Optional<TaskRuleEntity> exits = all_rules.get().stream().filter(x -> x.isSameRule(add_rule)).findFirst();
                if (exits.isPresent()) addList.remove(add_rule);
            }
        }
        if (CollectionUtils.isEmpty(addList)) return;
        super.batchInsert("batchInsert", addList);
        getCache().ifPresent(c -> c.evict("TASK_RULE_ALL"));
    }

    public void addRule(OrgEntity company, StoEntity store, BusinessType businessType, DelayType delayType, String delayTime,
                        SendChannel sendChannel, RoleType sendTarget, String template) {

        TaskRuleEntity saveInstance = null;
        if (store == null) {
            saveInstance = new TaskRuleEntity(company, businessType, delayType, delayTime, sendChannel, sendTarget, template);

        } else {
            saveInstance = new TaskRuleEntity(store, businessType, delayType, delayTime, sendChannel, sendTarget, template);
        }
        final TaskRuleEntity save_enity = saveInstance;
        Optional<List<TaskRuleEntity>> all_rules = findAll();
        all_rules.ifPresent(rules -> {
            Optional<TaskRuleEntity> exits = rules.stream().filter(x -> x.isSameRule(save_enity)).findFirst();
            Preconditions.checkState(!exits.isPresent(), "已经存在相同的规则，无法重复新增. %s", exits.orElse(null));
        });
        super.batchInsert("batchInsert", Lists.newArrayList(save_enity));
        getCache().ifPresent(c -> c.evict("TASK_RULE_ALL"));
    }

    public Optional<List<TaskRuleEntity>> findStoreRuleByType(StoEntity store, BusinessType businessType) {
        Optional<List<TaskRuleEntity>> all_rules = findAll();
        if (!all_rules.isPresent()) return Optional.empty();
        List<TaskRuleEntity> sub_list = all_rules.get().stream().filter(x -> x.isSameBusinessType(businessType))
                .filter(x -> x.isStore(store)).collect(Collectors.toList());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findStoreRuleByType(%d,%s) return %s", store.getId(), businessType, sub_list));
        return Optional.ofNullable(CollectionUtils.isNotEmpty(sub_list) ? sub_list : null);
    }

    public Optional<List<TaskRuleEntity>> findCompanyRuleByType(OrgEntity company, BusinessType businessType) {
        Optional<List<TaskRuleEntity>> all_rules = findAll();
        if (!all_rules.isPresent()) return Optional.empty();
        List<TaskRuleEntity> sub_list = all_rules.get().stream().filter(x -> x.isSameBusinessType(businessType))
                .filter(x -> x.isOnlyCompany(company)).collect(Collectors.toList());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findCompanyRuleByType(%d,%s) return %s", company.getId(), businessType, sub_list));
        return Optional.ofNullable(CollectionUtils.isNotEmpty(sub_list) ? sub_list : null);
    }

    public Optional<List<TaskRuleEntity>> findStoreByTaskSource(final TaskSourceEntity taskSource) {
        Optional<List<TaskRuleEntity>> rules = findAll();
        if (!rules.isPresent()) return Optional.empty();
        List<TaskRuleEntity> sub_rules = rules.get().stream().filter(TaskRuleEntity::isEnabled)
                .filter(x -> x.isSameBusinessType(taskSource)).filter(TaskRuleEntity::isStore).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_rules) ? null : sub_rules);
    }

    public Optional<List<TaskRuleEntity>> findCompanyByTaskSource(final TaskSourceEntity taskSource) {
        Optional<List<TaskRuleEntity>> rules = findAll();
        if (!rules.isPresent()) return Optional.empty();
        List<TaskRuleEntity> sub_rules = rules.get().stream().filter(TaskRuleEntity::isEnabled)
                .filter(x -> x.isSameBusinessType(taskSource)).filter(TaskRuleEntity::isCompany).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_rules) ? null : sub_rules);
    }

    @SuppressWarnings("unchecked")
    Optional<List<TaskRuleEntity>> findAll() {
        final String cacheKey = "TASK_RULE_ALL";
        if (getCache().isPresent()) {
            Object value = getCache().get().get(cacheKey, Object.class);
            if (null != value) return Optional.of((List<TaskRuleEntity>) value);
        }
        Optional<List<TaskRuleEntity>> list = super.queryForEntities("query4list", null, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAll(TaskRuleEntity) size is %s", list.map(List::size).orElse(0)));
        getCache().ifPresent(c -> list.ifPresent(l -> c.put(cacheKey, l)));
        return list;
    }

    @Override
    protected RowMapper<TaskRuleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<TaskRuleEntity> {
        @Override
        public TaskRuleEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TaskRuleEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
