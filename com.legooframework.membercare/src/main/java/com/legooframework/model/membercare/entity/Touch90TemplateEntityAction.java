package com.legooframework.model.membercare.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Touch90TemplateEntityAction extends BaseEntityAction<Touch90TemplateEntity> {

    private static final Logger logger = LoggerFactory.getLogger(Touch90TemplateEntityAction.class);
    private static final Comparator<Touch90TemplateEntity> TOUCH90_TEMPLATE_ORDERING = Comparator
            .comparingInt(value -> Integer.valueOf(value.getNodeId()));

    public Touch90TemplateEntityAction() {
        super("CrmJobsCache");
    }

    /**
     * 加载门店可用的 90
     *
     * @param store 门店
     * @return Touch90TemplateEntitys
     */
    public Optional<List<Touch90TemplateEntity>> loadByStore(CrmStoreEntity store) {
        Optional<List<Touch90TemplateEntity>> list = loadByCompanyId(store.getCompanyId());
        if (!list.isPresent()) return Optional.empty();
        List<Touch90TemplateEntity> sub_list = list.get().stream().filter(x -> x.hasStore(store))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sub_list)) sub_list.sort(TOUCH90_TEMPLATE_ORDERING);
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    /**
     * shi见驱动
     *
     * @param careRule       规则
     * @param incloudCompany 饱饭公司
     * @param stores         们带你们
     * @param user           用户  维保家国
     */
    public void saveOrUpdate(TaskCareRule4Touch90Entity careRule, boolean incloudCompany, Collection<CrmStoreEntity> stores,
                             LoginContext user) {
        Optional<List<Touch90TemplateEntity>> touch90_templates_opt = loadByCompany(user);
        List<Touch90TemplateEntity> touch90_templates = Lists.newArrayList();
        touch90_templates_opt.ifPresent(touch90_templates::addAll);
        List<TaskCareDetailRule> detailRules = careRule.getRuleDetails();
        detailRules.forEach(rule -> {
            Optional<Touch90TemplateEntity> exits = touch90_templates.stream().filter(x -> x.hasTemplate(careRule, rule)).findFirst();
            if (!exits.isPresent())
                touch90_templates.add(new Touch90TemplateEntity(rule, careRule.getCategories(), user));
        });
        List<Touch90TemplateEntity> touch90_templates_clone = touch90_templates.stream()
                .filter(x -> StringUtils.equals(x.getCategories(), careRule.getCategories()))
                .map(Touch90TemplateEntity::cloneSelf)
                .collect(Collectors.toList());
        touch90_templates_clone.forEach(temp -> {
            if (incloudCompany) temp.addByCompany();
            if (CollectionUtils.isNotEmpty(stores)) {
                stores.forEach(temp::addByStore);
            }
        });
        super.batchInsert("replaceIntoTemplates", touch90_templates_clone);
        getCache().ifPresent(c -> c.evict(String.format("%s_touch90_%s", getModelName(), user.getTenantId())));
    }

    public Optional<List<Touch90TemplateEntity>> loadByCompany(CrmOrganizationEntity company) {
        return loadByCompanyId(company.getId());
    }

    public Optional<List<Touch90TemplateEntity>> loadByCompany(LoginContext user) {
        return loadByCompanyId(user.getTenantId().intValue());
    }

    Optional<List<Touch90TemplateEntity>> loadByCompanyId(Integer companyId) {
        final String cache_key = String.format("%s_touch90_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<Touch90TemplateEntity> cache = getCache().get().get(cache_key, List.class);
            if (cache != null) return Optional.of(cache);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<Touch90TemplateEntity>> list = super.queryForEntities("findAllByCompanyId", params, getRowMapper());
        list.ifPresent(l -> l.sort(TOUCH90_TEMPLATE_ORDERING));
        getCache().ifPresent(c -> list.ifPresent(l -> c.put(cache_key, l)));
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) size is %s", companyId, list.map(List::size).orElse(0)));
        return list;
    }

    @Override
    protected RowMapper<Touch90TemplateEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<Touch90TemplateEntity> {
        @Override
        public Touch90TemplateEntity mapRow(ResultSet res, int i) throws SQLException {
            return new Touch90TemplateEntity(res.getString("id"), res);
        }
    }
}
