package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Touch90DefauteTemplate {

    private final Integer companyId, storeId;
    //subRuleId ||  categories || id :int|| template
    private final List<Map<String, Object>> templates;

    public Touch90DefauteTemplate(Integer companyId, Integer storeId, List<Map<String, Object>> templates) {
        this.companyId = companyId;
        this.storeId = storeId;
        this.templates = Lists.newArrayList(templates);
    }

    public Touch90DefauteTemplate(CrmOrganizationEntity company, CrmStoreEntity store, List<Map<String, Object>> templates) {
        this.companyId = company.getId();
        this.storeId = store == null ? -1 : store.getId();
        this.templates = ImmutableList.copyOf(templates);
    }

    public boolean hasTemplates() {
        return CollectionUtils.isNotEmpty(this.templates);
    }

    public Optional<List<Map<String, Object>>> findByCategories(String categories) {
        if (CollectionUtils.isEmpty(templates)) return Optional.empty();
        List<Map<String, Object>> sub_list = this.templates.stream().filter(map -> StringUtils
                .equals(categories, MapUtils.getString(map, "categories"))).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    public Optional<String> getTemplateById(String categories, String subRuleId) {
        int int_rule = Integer.valueOf(subRuleId);
        if (CollectionUtils.isEmpty(this.templates)) return Optional.empty();
        Optional<Map<String, Object>> param = this.templates.stream()
                .filter(x -> Objects.equal(categories, x.get("categories")))
                .filter(x -> Objects.equal(int_rule, x.get("id"))).findFirst();
        return param.map(map -> MapUtils.getString(map, "template"));
    }

    List<Map<String, Object>> getTemplates() {
        return templates;
    }

    public List<Map<String, Object>> getTemplatesByCategories(String categories) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(categories), "分类名称不可以为空值...");
        if (CollectionUtils.isEmpty(this.templates)) return null;
        return this.templates.stream().filter(map -> StringUtils.equals(categories, MapUtils.getString(map, "categories")))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("templates", templates)
                .toString();
    }
}
