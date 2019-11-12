package com.legooframework.model.takecare.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CareNinetyRuleEntityAction extends BaseEntityAction<CareNinetyRuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CareNinetyRuleEntityAction.class);

    public CareNinetyRuleEntityAction() {
        super(null);
    }

    public Optional<CareNinetyRuleEntity> loadByStore(StoEntity store) {
        Map<String, Object> params = store.toParamMap();
        params.put("sql", "findByStore");
        Optional<CareNinetyRuleEntity> store_rule = querySingleByParams(params);
        if (store_rule.isPresent()) return store_rule;
        params.put("sql", "findByCompany");
        return querySingleByParams(params);
    }

    public Optional<CareNinetyRuleEntity> loadByCompany(OrgEntity company) {
        Map<String, Object> params = company.toParamMap();
        params.put("sql", "findByCompany");
        return querySingleByParams(params);
    }

    public void saveByCompany(OrgEntity company, int toHour, int toNode1,
                              int toNode3, int toNode7, int toNode15, int toNode30, int toNode60, int toNode90,
                              String remark, int limitDays, double minAmount, double limitAmount,
                              int toHourDelay, int toNode1Delay, int toNode3Delay, int toNode7Delay,
                              int toNode15Delay, int toNode30Delay, int toNode60Delay, int toNode90Delay, double mergeAmount,
                              boolean appToStores) {
        CareNinetyRuleEntity instance = CareNinetyRuleEntity.createByCompany(company, toHour, toNode1,
                toNode3, toNode7, toNode15, toNode30, toNode60, toNode90, remark, limitDays, minAmount, limitAmount,
                toHourDelay, toNode1Delay, toNode3Delay, toNode7Delay,
                toNode15Delay, toNode30Delay, toNode60Delay, toNode90Delay, mergeAmount);
        Map<String, Object> params = company.toParamMap();
        params.put("sql", "findByCompany");
        Optional<CareNinetyRuleEntity> com_rule = querySingleByParams(params);
        if (com_rule.isPresent() && com_rule.get().isEnabled()) {
            disabledByEntity(com_rule.get());
        }
        super.batchInsert("batchInsert", Lists.newArrayList(instance));
        if (appToStores) {
            disabledAllStores(company);
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("saveByCompany(%d ,appToStores = %s) finshed", company.getId(), appToStores));
    }

    public void saveByStore(StoEntity store, int toHour, int toNode1,
                            int toNode3, int toNode7, int toNode15, int toNode30, int toNode60, int toNode90,
                            String remark, int limitDays, double minAmount, double limitAmount,
                            int toHourDelay, int toNode1Delay, int toNode3Delay, int toNode7Delay,
                            int toNode15Delay, int toNode30Delay, int toNode60Delay, int toNode90Delay, double mergeAmount) {
        CareNinetyRuleEntity instance = CareNinetyRuleEntity.createByStore(store, toHour, toNode1,
                toNode3, toNode7, toNode15, toNode30, toNode60, toNode90, remark, limitDays, minAmount, limitAmount,
                toHourDelay, toNode1Delay, toNode3Delay, toNode7Delay,
                toNode15Delay, toNode30Delay, toNode60Delay, toNode90Delay, mergeAmount);
        Map<String, Object> params = store.toParamMap();
        params.put("sql", "findByStore");
        Optional<CareNinetyRuleEntity> store_rule = querySingleByParams(params);
        if (store_rule.isPresent() && store_rule.get().isEnabled()) {
            disabledByEntity(store_rule.get());
        }
        super.batchInsert("batchInsert", Lists.newArrayList(instance));
    }

    public void saveByStores(Collection<StoEntity> stores, int toHour, int toNode1,
                             int toNode3, int toNode7, int toNode15, int toNode30, int toNode60, int toNode90,
                             String remark, int limitDays, double minAmount, double limitAmount,
                             int toHourDelay, int toNode1Delay, int toNode3Delay, int toNode7Delay,
                             int toNode15Delay, int toNode30Delay, int toNode60Delay, int toNode90Delay, double mergeAmount) {
        if (CollectionUtils.isEmpty(stores)) return;
        disabledAllStores(stores);
        List<CareNinetyRuleEntity> rules = stores.stream().map(x -> CareNinetyRuleEntity.createByStore(x, toHour, toNode1,
                toNode3, toNode7, toNode15, toNode30, toNode60, toNode90, remark, limitDays, minAmount, limitAmount,
                toHourDelay, toNode1Delay, toNode3Delay, toNode7Delay,
                toNode15Delay, toNode30Delay, toNode60Delay, toNode90Delay, mergeAmount))
                .collect(Collectors.toList());
        super.batchInsert("batchInsert", Lists.newArrayList(rules));
    }

    private void disabledAllStores(Collection<StoEntity> stores) {
        if (CollectionUtils.isEmpty(stores)) return;
        List<Integer> storeIds = stores.stream().mapToInt(BaseEntity::getId).boxed().collect(Collectors.toList());
        String update_sql = String.format("UPDATE acp.crm_90_node_map SET enable=0 WHERE enable=1 AND store_id IN (%s)",
                Joiner.on(',').join(storeIds));
        Objects.requireNonNull(getJdbcTemplate()).update(update_sql);
    }

    private void disabledAllStores(OrgEntity company) {
        Objects.requireNonNull(getJdbcTemplate()).update("UPDATE acp.crm_90_node_map SET enable=0 WHERE enable=1 AND store_id <> 0 AND company_id = ?",
                company.getId());
    }

    private void disabledByEntity(CareNinetyRuleEntity rule) {
        Objects.requireNonNull(getJdbcTemplate()).update("UPDATE acp.crm_90_node_map SET enable=0 WHERE enable =1 AND store_id= ? AND company_id = ?",
                rule.getStoreId(), rule.getCompanyId());
    }

    private Optional<CareNinetyRuleEntity> querySingleByParams(Map<String, Object> params) {
        Optional<List<CareNinetyRuleEntity>> list = queryForEntities("query4list", params, getRowMapper());
        return list.map(x -> x.get(0));
    }


    @Override
    protected RowMapper<CareNinetyRuleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<CareNinetyRuleEntity> {
        @Override
        public CareNinetyRuleEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new CareNinetyRuleEntity(resultSet);
        }
    }
}

