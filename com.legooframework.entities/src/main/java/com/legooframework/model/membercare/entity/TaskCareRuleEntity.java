package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TaskCareRuleEntity extends BaseEntity<String> implements BatchSetter {

    private final BusinessType businessType;
    private String categories = "0", categoriesName = "通用90规则";
    private final Integer companyId;
    private Integer storeId;
    private boolean enabled;
    private String mergeBuilderSpec;
    private String ruleBuilderSpec;
    private String autoRunBuilderSpec;
    private final static String KEY_MERGEBUILDERSPEC = "mergeBuilderSpec";
    private final static String KEY_RULEBUILDERSPEC = "ruleBuilderSpec";
    private final static String KEY_AUTORUNBUILDERSPEC = "autoRunBuilderSpec";

    public Optional<AutoRunChannel> getAutoRunChannel() {
        if (Strings.isNullOrEmpty(autoRunBuilderSpec)) return Optional.empty();
        Splitter.MapSplitter MAP_SPLITTER = Splitter.on(',').trimResults().withKeyValueSeparator('=');
        Map<String, String> params = MAP_SPLITTER.split(autoRunBuilderSpec);
        int channel = MapUtils.getInteger(params, "channel", -1);
        return Optional.ofNullable(-1 == channel ? null : AutoRunChannel.parse(channel));
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("id", getId());
        params.put("categoriesName", categoriesName);
        params.put("businessType", businessType.toString());
        params.put("categories", getCategories());
        params.put("enabled", enabled);
        if (!Strings.isNullOrEmpty(mergeBuilderSpec)) params.put(KEY_MERGEBUILDERSPEC, mergeBuilderSpec);
        params.put(KEY_RULEBUILDERSPEC, ruleBuilderSpec);
        if (StringUtils.isNotEmpty(autoRunBuilderSpec)) params.put(KEY_AUTORUNBUILDERSPEC, autoRunBuilderSpec);
        return params;
    }

    private static String generateId(BusinessType businessType, String categories, Integer companyId, Integer storeId) {
        return String.format("%s_%s_%s_%s", companyId, storeId == null ? -1 : storeId, businessType.toString(),
                categories == null ? "0" : categories);
    }

    void setAutoRunBuilderSpec(String autoRunBuilderSpec) {
        this.autoRunBuilderSpec = autoRunBuilderSpec;
    }

    TaskCareRuleEntity(Integer companyId, Integer storeId, BusinessType businessType, String categories, String ruleBuilderSpec,
                       boolean enabled, Long userId) {
        super(generateId(businessType, categories, companyId, storeId), companyId.longValue(), userId);
        this.businessType = businessType;
        this.companyId = companyId;
        this.storeId = storeId;
        this.ruleBuilderSpec = ruleBuilderSpec;
        this.enabled = enabled;
        this.categories = Strings.isNullOrEmpty(categories) ? "0" : categories;
    }

    void setMergeBuilderSpec(String mergeBuilderSpec) {
        this.mergeBuilderSpec = mergeBuilderSpec;
    }

    TaskCareRuleEntity(TaskCareRuleEntity taskCareRule) {
        super(taskCareRule.getId(), taskCareRule.getTenantId(), taskCareRule.getCreator());
        this.businessType = taskCareRule.getBusinessType();
        this.companyId = taskCareRule.getCompanyId();
        this.storeId = taskCareRule.getStoreId();
        this.enabled = taskCareRule.isEnabled();
        this.autoRunBuilderSpec = taskCareRule.autoRunBuilderSpec;
        this.mergeBuilderSpec = taskCareRule.mergeBuilderSpec;
        this.ruleBuilderSpec = taskCareRule.ruleBuilderSpec;
        this.categories = taskCareRule.getCategories();
        this.categoriesName = taskCareRule.categoriesName;
    }

    TaskCareRuleEntity(String id, ResultSet res) {
        super(id, res);
        try {
            String _businessType = ResultSetUtil.getString(res, "businessType");
            this.businessType = BusinessType.parse(_businessType);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
            this.mergeBuilderSpec = ResultSetUtil.getOptString(res, KEY_MERGEBUILDERSPEC, null);
            this.ruleBuilderSpec = ResultSetUtil.getOptString(res, KEY_RULEBUILDERSPEC, null);
            this.autoRunBuilderSpec = ResultSetUtil.getOptString(res, KEY_AUTORUNBUILDERSPEC, null);
            this.categories = ResultSetUtil.getOptString(res, "categories", "0");
            this.categoriesName = ResultSetUtil.getOptString(res, "categoriesName", "通用规则");
        } catch (SQLException e) {
            throw new RuntimeException("Restore TaskCareRuleEntity has SQLException", e);
        }
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // company_id, store_id, business_type, enbaled, content, details , tenant_id, creator
        ps.setObject(1, getId());
        ps.setObject(2, getCompanyId());
        ps.setObject(3, this.storeId == null ? -1 : getStoreId());
        ps.setObject(4, businessType.toString());
        ps.setObject(5, categories);
        ps.setObject(6, enabled ? 1 : 0);
        ps.setObject(7, this.mergeBuilderSpec);
        ps.setObject(8, this.ruleBuilderSpec);
        ps.setObject(9, this.autoRunBuilderSpec);
        ps.setObject(10, getCompanyId());
        ps.setObject(11, getCreator());
        ps.setObject(12, UUID.randomUUID().toString());
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    String getCategories() {
        return categories == null ? "0" : categories;
    }

    boolean isCategories(String categories) {
        String _categories = categories == null ? "0" : categories;
        return StringUtils.equals(this.getCategories(), _categories);
    }

    String getAutoRunBuilderSpec() {
        return autoRunBuilderSpec;
    }

    boolean isOpenMerge() {
        return StringUtils.isNotEmpty(mergeBuilderSpec);
    }

    boolean isEnabled() {
        return enabled;
    }

    Optional<TaskCareRuleEntity> disabled() {
        if (!isEnabled()) return Optional.empty();
        TaskCareRuleEntity clone = (TaskCareRuleEntity) cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    String getMergeBuilderSpec() {
        return mergeBuilderSpec;
    }

    String getRuleBuilderSpec() {
        return ruleBuilderSpec;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    boolean isBelongCompany(CrmOrganizationEntity company) {
        return this.companyId.equals(company.getId());
    }

    TaskCareRuleEntity setStoreId(Integer storeId) {
        TaskCareRuleEntity clone = (TaskCareRuleEntity) cloneMe();
        clone.storeId = storeId;
        return clone;
    }

    boolean isStore(CrmStoreEntity store) {
        return this.companyId.equals(store.getCompanyId()) && this.storeId.equals(store.getId());
    }

    boolean isStore() {
        return this.storeId != -1;
    }

    boolean isStoreById(Integer storeId) {
        return this.storeId.equals(storeId);
    }

    boolean isCompany() {
        return this.companyId != -1 && this.storeId == -1;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("businessType", businessType.toString());
        params.put("categories", getCategories());
        params.put("enabled", enabled ? 1 : 0);
        params.put(KEY_MERGEBUILDERSPEC, mergeBuilderSpec);
        params.put(KEY_RULEBUILDERSPEC, ruleBuilderSpec);
        params.put(KEY_AUTORUNBUILDERSPEC, autoRunBuilderSpec);
        return params;
    }

    boolean equalsByRule(TaskCareRuleEntity that) {
        if (this == that) return true;
        return businessType == that.businessType &&
                Objects.equal(categories, that.categories) &&
                Objects.equal(autoRunBuilderSpec, that.autoRunBuilderSpec) &&
                Objects.equal(mergeBuilderSpec, that.mergeBuilderSpec) &&
                Objects.equal(ruleBuilderSpec, that.ruleBuilderSpec);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCareRuleEntity that = (TaskCareRuleEntity) o;
        return enabled == that.enabled &&
                businessType == that.businessType &&
                Objects.equal(categories, that.categories) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(mergeBuilderSpec, that.mergeBuilderSpec) &&
                Objects.equal(autoRunBuilderSpec, that.autoRunBuilderSpec) &&
                Objects.equal(ruleBuilderSpec, that.ruleBuilderSpec);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), businessType, categories, companyId, storeId,
                enabled, mergeBuilderSpec, ruleBuilderSpec, autoRunBuilderSpec);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("RULE")
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("businessType", businessType)
                .add("categories", categories)
                .add("mergeBuilderSpec", mergeBuilderSpec)
                .add("ruleBuilderSpec", ruleBuilderSpec)
                .add("autoRunBuilderSpec", autoRunBuilderSpec)
                .toString();
    }
}
