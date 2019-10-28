package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmpDividedRuleEntityAction extends BaseEntityAction<EmpDividedRuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(EmpDividedRuleEntityAction.class);

    public EmpDividedRuleEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public void insert4Store(StoEntity store, boolean autoRun, List<List<EmpDividedRuleEntity.Divided>> memberRule,
                             List<List<EmpDividedRuleEntity.Divided>> noMemberRule) {
        EmpDividedRuleEntity rule = EmpDividedRuleEntity.createByStore(store, autoRun, memberRule, noMemberRule);
        super.updateAction(rule, "insert");
        evict(store.getCompanyId());
    }

    public void insert4Company(OrgEntity company, boolean autoRun, List<List<EmpDividedRuleEntity.Divided>> memberRule,
                               List<List<EmpDividedRuleEntity.Divided>> noMemberRule,
                               List<List<EmpDividedRuleEntity.Divided>> crossMemberRule,
                               List<List<EmpDividedRuleEntity.Divided>> crossNoMemberRule, boolean coverted) {
        EmpDividedRuleEntity rule = EmpDividedRuleEntity.createByCompany(company, autoRun, memberRule, noMemberRule, crossMemberRule,
                crossNoMemberRule);
        super.updateAction(rule, "insert");
        if (coverted) {
            super.updateAction("deleteByCompany", company.toParamMap());
            if (logger.isDebugEnabled())
                logger.debug(String.format("公司 %s 重写规则，应用到下级所有门店....", company.getId()));
        }
        evict(company.getId());
    }

    /**
     * 加载指定store  的规则
     *
     * @param store
     * @return
     */
    Optional<EmpDividedRuleAgg> findByStore(StoEntity store) {
        Optional<List<EmpDividedRuleEntity>> all_list = loadAllByCompany(store.getCompanyId());
        if (!all_list.isPresent()) return Optional.empty();
        Optional<EmpDividedRuleEntity> store_rule = all_list.get().stream().filter(x -> x.isStore(store)).findFirst();
        Optional<EmpDividedRuleEntity> com_rule = all_list.get().stream().filter(x -> x.isOnlyCompany(store)).findFirst();
        Preconditions.checkState(com_rule.isPresent(), "数据异常，缺失公司分成规则");
        return Optional.of(new EmpDividedRuleAgg(store, store_rule.orElse(null), com_rule.get()));
    }

    @SuppressWarnings("unchecked")
    Optional<List<EmpDividedRuleEntity>> loadAllByCompany(Integer companyId) {
        final String cache_key = String.format("EMP_DIVIDED_COM_%d", companyId);
        if (getCache().isPresent()) {
            Object cacheVal = getCache().get().get(cache_key, Object.class);
            if (cacheVal != null) return Optional.of((List<EmpDividedRuleEntity>) cacheVal);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "loadAllByCompany");
        params.put("companyId", companyId);
        Optional<List<EmpDividedRuleEntity>> list = queryForEntities("quer4List", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompany(%d) return size is %d", companyId, list.map(List::size).orElse(0)));
        getCache().ifPresent(c -> list.ifPresent(v -> c.put(cache_key, v)));
        return list;
    }

    public void insert4Org(Collection<StoEntity> stores, boolean autoRun, List<List<EmpDividedRuleEntity.Divided>> memberRule,
                           List<List<EmpDividedRuleEntity.Divided>> noMemberRule) {
        if (CollectionUtils.isEmpty(stores)) return;
        List<EmpDividedRuleEntity> rulse = Lists.newArrayListWithCapacity(stores.size());
        stores.forEach(store -> rulse.add(EmpDividedRuleEntity.createByStore(store, autoRun, memberRule, noMemberRule)));
        super.batchInsert("batchInsert", rulse);
        evict(stores.iterator().next().getCompanyId());
    }

    private void evict(Integer companyId) {
        getCache().ifPresent(c -> c.evict(companyId));
    }

    @Override
    protected RowMapper<EmpDividedRuleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<EmpDividedRuleEntity> {
        @Override
        public EmpDividedRuleEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new EmpDividedRuleEntity(resultSet.getInt("id"), resultSet);
        }
    }

}
