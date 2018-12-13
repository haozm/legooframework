package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CareRuleEntityAction extends BaseEntityAction<AbstractCareRuleRule> {

    public CareRuleEntityAction() {
        super("CrmJobsCache");
    }

    @Deprecated
    @Override
    public Optional<AbstractCareRuleRule> findById(Object id) {
        throw new UnsupportedOperationException("不支持该方法调用...");
    }

    /**
     * 返回指定门店的 90 生成规则
     *
     * @param store 门店
     * @return Touch90CareRuleEntity
     */
    public void saveOrUpdate90Rule(CrmOrganizationEntity company, CrmStoreEntity store,
                                   boolean enabled, boolean automatic, int maxConsumptionDays,
                                   int maxAmountOfconsumption, String details) {
        Preconditions.checkState(maxConsumptionDays > 0);
        Preconditions.checkState(maxAmountOfconsumption > 0);
        Touch90CareRuleEntity touch90 = new Touch90CareRuleEntity(company, store, automatic, enabled,
                maxConsumptionDays, maxAmountOfconsumption, details);
        final String cache_key = String.format("%s_touch90_%s", getModelName(), company.getId());
        super.updateAction("insert", touch90.toParamMap());
        getCache().ifPresent(x -> x.evict(cache_key));
    }

    public Optional<List<Touch90CareRuleEntity>> loadAllTouch90Rules(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company);
        return this.loadAllTouch90Rules(company.getId());
    }

    @SuppressWarnings("unchecked")
    private Optional<List<Touch90CareRuleEntity>> loadAllTouch90Rules(Integer companyId) {
        Preconditions.checkNotNull(companyId);
        final String cache_key = String.format("%s_touch90_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            List<Touch90CareRuleEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<Touch90CareRuleEntity>> touch90Rules = super
                .queryForEntities("loadTouch90Rules", params, new Touch90RowMapperImpl());
        if (!touch90Rules.isPresent()) return Optional.empty();
        if (getCache().isPresent()) getCache().get().put(cache_key, touch90Rules.get());
        return touch90Rules;
    }

    @Override
    protected RowMapper<AbstractCareRuleRule> getRowMapper() {
        return null;
    }

    class Touch90RowMapperImpl implements RowMapper<Touch90CareRuleEntity> {
        @Override
        public Touch90CareRuleEntity mapRow(ResultSet res, int i) throws SQLException {
            int storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            int companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            boolean enabled = ResultSetUtil.getObject(res, "enabled", Integer.class) == 1;
            boolean automatic = ResultSetUtil.getObject(res, "automatic", Integer.class) == 1;
            return new Touch90CareRuleEntity(res, enabled, automatic, storeId, companyId);
        }
    }
}
