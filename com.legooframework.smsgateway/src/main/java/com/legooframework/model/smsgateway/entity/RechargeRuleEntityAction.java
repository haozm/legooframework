package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RechargeRuleEntityAction extends BaseEntityAction<RechargeRuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RechargeRuleEntityAction.class);

    public RechargeRuleEntityAction() {
        super("smsGateWayCache");
    }

    public Optional<RechargeRuleEntity> findById(String id) {
        Optional<List<RechargeRuleEntity>> list = loadAllRules();
        return list.flatMap(re -> re.stream()
                .filter(x -> x.isEnabled() && x.getId().equals(id)).findFirst());
    }

    public Optional<RechargeRuleEntity> findTempById(String id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        return queryForEntity("findById", params, new RowMapperImpl());
    }

    public RechargeRuleSet loadAllRuleSet() {
        Optional<List<RechargeRuleEntity>> list = loadAllEnabledRules();
        Preconditions.checkState(list.isPresent(), "尚未配置计费规则....");
        return new RechargeRuleSet(list.get());
    }

    /**
     * 增加新规则
     *
     * @param min       范围
     * @param max       范围
     * @param unitPrice 单价
     * @param company   公司
     * @param remarks   备注
     * @return 字串
     */
    public String addRule(Long min, Long max, double unitPrice, OrgEntity company, String remarks, LocalDate expiredDate) {
        RechargeRuleEntity instance;
        if (null == company) {
            instance = new RechargeRuleEntity(min, max, unitPrice, null, null, false, expiredDate);
        } else {
            instance = new RechargeRuleEntity(min, max, unitPrice, company, remarks, false, expiredDate);
        }
        super.updateAction(instance, "insert");
        final String cache_key = String.format("%s_load_all", getModelName());
        getCache().ifPresent(c -> c.evict(cache_key));
        return instance.getId();
    }

    /**
     * 增加一次性临时规则
     *
     * @param min       范围
     * @param max       范围
     * @param unitPrice 单价
     * @param company   公司
     * @param remarks   备注
     * @return 字串 春梦
     */
    public String addTemporaryRule(Long min, Long max, double unitPrice, OrgEntity company, String remarks, LocalDate expiredDate) {
        Preconditions.checkNotNull(company, "入参 CrmOrganizationEntity company 不可以为空值...");
        RechargeRuleEntity instance = new RechargeRuleEntity(min, max, unitPrice, company, remarks, true,
                expiredDate);
        super.updateAction(instance, "insert");
        return instance.getId();
    }

    /**
     * 禁用其规则孙悟空
     *
     * @param ruleId 规则ID
     */
    public void disabled(String ruleId) {
        Optional<List<RechargeRuleEntity>> list = loadAllRules();
        if (!list.isPresent()) return;
        Optional<RechargeRuleEntity> exits = list.get().stream().filter(x -> x.getId().equals(ruleId)).findFirst();
        Preconditions.checkState(exits.isPresent(), "不存在 ruleId=%s 对应的规则定义", ruleId);
        Optional<RechargeRuleEntity> clone = exits.get().disabled();
        clone.ifPresent(c -> {
            super.updateAction(c, "changeState");
            getCache().ifPresent(cahce -> cahce.evict(String.format("%s_load_all", getModelName())));
        });
    }

    /**
     * 启用其规则孙悟空
     *
     * @param ruleId 规则ID
     */
    public void enabled(String ruleId) {
        Optional<List<RechargeRuleEntity>> list = loadAllRules();
        if (!list.isPresent()) return;
        Optional<RechargeRuleEntity> exits = list.get().stream().filter(x -> x.getId().equals(ruleId)).findFirst();
        Preconditions.checkState(exits.isPresent(), "不存在 ruleId=%s 对应的规则定义", ruleId);
        Optional<RechargeRuleEntity> clone = exits.get().enabled();
        clone.ifPresent(c -> {
            super.updateAction(c, "changeState");
            getCache().ifPresent(cahce -> cahce.evict(String.format("%s_load_all", getModelName())));
        });
    }

    private Optional<List<RechargeRuleEntity>> loadAllEnabledRules() {
        Optional<List<RechargeRuleEntity>> all_rules = loadAllRules();
        if (!all_rules.isPresent()) return Optional.empty();
        List<RechargeRuleEntity> enabled_list = all_rules.get().stream().filter(RechargeRuleEntity::isNotExpired)
                .filter(RechargeRuleEntity::isEnabled)
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(enabled_list) ? null : enabled_list);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<RechargeRuleEntity>> loadAllRules() {
        final String cache_key = String.format("%s_load_all", getModelName());
        if (getCache().isPresent()) {
            List<RechargeRuleEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list))
                return Optional.of(list);
        }
        Optional<List<RechargeRuleEntity>> list_opt = super.queryForEntities("loadAllRule", null, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllRule() size is %s", list_opt.map(List::size).orElse(0)));
        list_opt.ifPresent(r -> getCache().ifPresent(c -> c.put(cache_key, r)));
        return list_opt;
    }

    @Override
    protected RowMapper<RechargeRuleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<RechargeRuleEntity> {
        @Override
        public RechargeRuleEntity mapRow(ResultSet res, int i) throws SQLException {
            return new RechargeRuleEntity(res.getString("id"), res);
        }
    }
}
