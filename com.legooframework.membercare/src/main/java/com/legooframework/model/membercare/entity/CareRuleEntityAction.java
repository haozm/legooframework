package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param
     * @return Touch90CareRuleEntity
     */
    public void saveOrUpdate90Rule(CrmOrganizationEntity company, Collection<CrmStoreEntity> stores,
                                   boolean enabled, boolean automatic, int maxConsumptionDays,
                                   int maxAmountOfconsumption, boolean concalBefore, String details,
                                   boolean rewirte) {
        Preconditions.checkNotNull(company, "公司信息不可以为空...");
        Preconditions.checkState(maxConsumptionDays >= 0);
        Preconditions.checkState(maxAmountOfconsumption >= 0);
        List<Touch90CareRuleEntity> un_save_list = Lists.newArrayList();
        Touch90CareRuleEntity touch90Rule = new Touch90CareRuleEntity(company, automatic, enabled,
                maxConsumptionDays, maxAmountOfconsumption, concalBefore, details);
        if (CollectionUtils.isEmpty(stores)) {
            un_save_list.add(touch90Rule);
            if (rewirte) {
                Map<String, Object> params = Maps.newHashMap();
                params.put("companyId", company.getId());
                params.put("taskType", TaskType.Touche90.getValue());
                super.updateAction("deleteByCompany", params);
            }
        } else {
            if (rewirte) {
                stores.forEach(x -> un_save_list.add(touch90Rule.buildStore(x)));
            } else {
                Set<Integer> list_4_save_ids = stores.stream().map(BaseEntity::getId).collect(Collectors.toSet());
                Optional<List<Touch90CareRuleEntity>> rules = loadTouch90Rules(company);
                if (rules.isPresent()) {
                    Set<Integer> list_4_remove = rules.get().stream().map(Touch90CareRuleEntity::getStoreId)
                            .collect(Collectors.toSet());
                    list_4_save_ids.removeAll(list_4_remove);
                }
                if (CollectionUtils.isEmpty(list_4_save_ids)) return;
                List<CrmStoreEntity> list_4_save = Lists.newArrayList();
                stores.forEach(x -> {
                    if (list_4_save_ids.contains(x.getId())) list_4_save.add(x);
                });
                list_4_save.forEach(x -> un_save_list.add(touch90Rule.buildStore(x)));
            }
        }
        super.batchInsert("batchInsertOrUpdate", un_save_list);
        String cache_key = String.format("%s_touch90_%s", getModelName(), company.getId());
        getCache().ifPresent(x -> x.evict(cache_key));
    }

    /**
     * 获取门店指定的touch90 规则
     *
     * @param company
     * @param store
     * @return
     */
    public Touch90CareRuleEntity loadRuleByStore(CrmOrganizationEntity company, CrmStoreEntity store) {
        Optional<List<Touch90CareRuleEntity>> touch90CareRules = loadTouch90Rules(company);
        Preconditions.checkState(touch90CareRules.isPresent(), "公司 %s 无 touch90 设置...", company.getName());
        if (store == null) {
            Optional<Touch90CareRuleEntity> opt = touch90CareRules.get().stream()
                    .filter(Touch90CareRuleEntity::isOnlyCompany)
                    .findFirst();
            Preconditions.checkState(opt.isPresent(), "公司 %s 任务touch90的设置缺失...", company.getName());
            return opt.get();
        } else {
            int[] _args = new int[]{-1, store.getId()};
            List<Touch90CareRuleEntity> sub_list = touch90CareRules.get().stream()
                    .filter(x -> ArrayUtils.contains(_args, x.getStoreId()))
                    .collect(Collectors.toList());
            Optional<Touch90CareRuleEntity> touch90_store = sub_list.stream()
                    .filter(x -> x.getStoreId().equals(store.getId())).findFirst();
            return touch90_store.orElseGet(() -> sub_list.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<List<Touch90CareRuleEntity>> loadTouch90Rules(final CrmOrganizationEntity company) {
        final String cache_key = String.format("%s_touch90_%s", getModelName(), company.getId());
        if (getCache().isPresent()) {
            List<Touch90CareRuleEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskType", TaskType.Touche90.getValue());
        params.put("companyId", company.getId());
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
