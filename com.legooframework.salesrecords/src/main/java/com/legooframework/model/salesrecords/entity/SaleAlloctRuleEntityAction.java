package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SaleAlloctRuleEntityAction extends BaseEntityAction<SaleAlloctRuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SaleAlloctRuleEntityAction.class);

    public SaleAlloctRuleEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public void insert4Store(StoEntity store, boolean autoRun, List<List<SaleAlloctRuleEntity.Rule>> memberRule,
                             List<List<SaleAlloctRuleEntity.Rule>> noMemberRule) {
        SaleAlloctRuleEntity rule = SaleAlloctRuleEntity.createByStore(store, autoRun, memberRule, noMemberRule);
        Optional<List<SaleAlloctRuleEntity>> all_rule = loadAllByCompany(store.getCompanyId());
        if (all_rule.isPresent()) {
            Optional<SaleAlloctRuleEntity> exits = all_rule.get().stream().filter(x -> x.isSameRule(rule)).findFirst();
            exits.ifPresent(this::deleteByRule);
        }
        super.updateAction(rule, "insert");
        evict(store.getCompanyId());
    }

    private void deleteByRule(SaleAlloctRuleEntity rule) {
        Objects.requireNonNull(super.getJdbcTemplate())
                .update("UPDATE acp.ACP_EMPLOYEE_ALLOT_RULE SET delete_flag = 1 WHERE id = ?", rule.getId());
    }

    private void deleteAllStore(SaleAlloctRuleEntity rule) {
        Objects.requireNonNull(super.getJdbcTemplate())
                .update("UPDATE acp.ACP_EMPLOYEE_ALLOT_RULE SET delete_flag = 1 WHERE company_id = ? AND store_id != 0",
                        rule.getCompanyId());
    }

    public void insert4Company(OrgEntity company, boolean autoRun, List<List<SaleAlloctRuleEntity.Rule>> memberRule,
                               List<List<SaleAlloctRuleEntity.Rule>> noMemberRule,
                               List<List<SaleAlloctRuleEntity.Rule>> crossMemberRule,
                               List<List<SaleAlloctRuleEntity.Rule>> crossNoMemberRule, boolean coverted, LocalDate startDate) {
        SaleAlloctRuleEntity rule = SaleAlloctRuleEntity.createByCompany(company, autoRun, memberRule, noMemberRule,
                crossMemberRule, crossNoMemberRule, startDate);
        Optional<List<SaleAlloctRuleEntity>> all_rule = loadAllByCompany(company.getId());
        if (all_rule.isPresent()) {
            Optional<SaleAlloctRuleEntity> exits = all_rule.get().stream().filter(x -> x.isSameRule(rule)).findFirst();
            exits.ifPresent(this::deleteById);
        }
        super.updateAction(rule, "insert");
        if (coverted) {
            deleteAllStore(rule);
            if (logger.isDebugEnabled())
                logger.debug(String.format("公司 %s 重写规则，应用到下级所有门店....", company.getId()));
        }
        evict(company.getId());
    }

    public Optional<List<Map<String, Object>>> loadEnabledCompanies() {
        String query_sql = "SELECT company_id AS 'companyId', DATE_FORMAT(start_date,GET_FORMAT(DATE,'iso')) AS 'startDate' \n " +
                "FROM acp.ACP_EMPLOYEE_ALLOT_RULE WHERE delete_flag = 0 AND store_id = 0";
        Optional<List<Map<String, Object>>> mapList = super.queryForMapList(query_sql, null);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEnabledCompanies() res->%s", mapList.orElse(null)));
        return mapList;
    }

    /**
     * 加载指定store  的规则
     *
     * @param store
     * @return
     */
    public Optional<SaleAlloctRule4Store> findByStore4Use(StoEntity store) {
        Optional<List<SaleAlloctRuleEntity>> all_list = loadAllByCompany(store.getCompanyId());
        if (!all_list.isPresent()) return Optional.empty();
        Optional<SaleAlloctRuleEntity> store_rule = all_list.get().stream().filter(x -> x.isStore(store)).findFirst();
        Optional<SaleAlloctRuleEntity> com_rule = all_list.get().stream().filter(x -> x.isOnlyCompany(store)).findFirst();
        Preconditions.checkState(com_rule.isPresent(), "数据异常，缺失公司分成规则");
        return Optional.of(new SaleAlloctRule4Store(store, store_rule.orElse(null), com_rule.get()));
    }

    public Optional<SaleAlloctRuleEntity> findByCompany(OrgEntity company) {
        Optional<List<SaleAlloctRuleEntity>> all_list = loadAllByCompany(company.getId());
        return all_list.flatMap(sa -> sa.stream().filter(x -> x.isOnlyCompany(company)).findFirst());
    }

    public Optional<SaleAlloctRuleEntity> findByStore(StoEntity store) {
        Optional<List<SaleAlloctRuleEntity>> all_list = loadAllByCompany(store.getCompanyId());
        if (!all_list.isPresent()) return Optional.empty();
        Optional<SaleAlloctRuleEntity> store_rule = all_list.get().stream().filter(x -> x.isStore(store)).findFirst();
        if (store_rule.isPresent()) return store_rule;
        return all_list.get().stream().filter(x -> x.isOnlyCompany(store)).findFirst();
    }

    @SuppressWarnings("unchecked")
    Optional<List<SaleAlloctRuleEntity>> loadAllByCompany(Integer companyId) {
        final String cache_key = String.format("EMP_DIVIDED_COM_%d", companyId);
        if (getCache().isPresent()) {
            Object cacheVal = getCache().get().get(cache_key, Object.class);
            if (cacheVal != null) return Optional.of((List<SaleAlloctRuleEntity>) cacheVal);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "loadAllByCompany");
        params.put("companyId", companyId);
        Optional<List<SaleAlloctRuleEntity>> list = queryForEntities("quer4List", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompany(%d) return size is %d", companyId, list.map(List::size).orElse(0)));
        getCache().ifPresent(c -> list.ifPresent(v -> c.put(cache_key, v)));
        return list;
    }

    public void insert4Org(Collection<StoEntity> stores, boolean autoRun, List<List<SaleAlloctRuleEntity.Rule>> memberRule,
                           List<List<SaleAlloctRuleEntity.Rule>> noMemberRule) {
        if (CollectionUtils.isEmpty(stores)) return;
        List<SaleAlloctRuleEntity> rulse = Lists.newArrayListWithCapacity(stores.size());
        stores.forEach(store -> rulse.add(SaleAlloctRuleEntity.createByStore(store, autoRun, memberRule, noMemberRule)));
        super.batchInsert("batchInsert", rulse);
        evict(stores.iterator().next().getCompanyId());
    }

    private void evict(Integer companyId) {
        getCache().ifPresent(c -> c.evict(String.format("EMP_DIVIDED_COM_%d", companyId)));
    }

    @Override
    protected RowMapper<SaleAlloctRuleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SaleAlloctRuleEntity> {
        @Override
        public SaleAlloctRuleEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new SaleAlloctRuleEntity(resultSet.getInt("id"), resultSet);
        }
    }

}
