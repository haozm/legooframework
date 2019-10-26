package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Touch90RuleDifference {

    private String subRuleId;
    // 0:删除子节点 1:子节点禁用节点 2：子节点启用节点 3:启用修改时间 4:大规则禁用 5： 大规则启用 6:closeAuto 7:openAuto 8:删除大节点
    private final int action;
    private LocalTime localTime;
    private final TaskCareRule4Touch90Entity careRule;
    private List<CrmStoreEntity> stores;
    private boolean incloudCompany;

    private Touch90RuleDifference(TaskCareRule4Touch90Entity careRule, int action, String subRuleId, LocalTime localTime) {
        this.careRule = careRule;
        this.action = action;
        this.subRuleId = subRuleId;
        this.localTime = localTime;
    }

    private Touch90RuleDifference(TaskCareRule4Touch90Entity careRule, int action, boolean incloudCompany,
                                  Collection<CrmStoreEntity> stores) {
        this.careRule = careRule;
        this.action = action;
        this.incloudCompany = incloudCompany;
        if (CollectionUtils.isNotEmpty(stores))
            this.stores = Lists.newArrayList(stores);
    }

    public boolean isIncloudCompany() {
        return incloudCompany;
    }

    static Touch90RuleDifference nodeCloseAuto(TaskCareRule4Touch90Entity careRule, String subRuleId) {
        return new Touch90RuleDifference(careRule, 6, subRuleId, null);
    }

    static Touch90RuleDifference nodeOpenAuto(TaskCareRule4Touch90Entity careRule, String subRuleId, LocalTime localTime) {
        return new Touch90RuleDifference(careRule, 7, subRuleId, localTime);
    }

    static Touch90RuleDifference nodeRemoves(TaskCareRule4Touch90Entity careRule, String subRuleId) {
        return new Touch90RuleDifference(careRule, 0, subRuleId, null);
    }

    static Touch90RuleDifference nodeAdd(TaskCareRule4Touch90Entity careRule, String subRuleId) {
        return new Touch90RuleDifference(careRule, 9, subRuleId, null);
    }

    static Touch90RuleDifference nodeDisabled(TaskCareRule4Touch90Entity careRule, String subRuleId) {
        return new Touch90RuleDifference(careRule, 1, subRuleId, null);
    }

    static Touch90RuleDifference nodeEnabled(TaskCareRule4Touch90Entity careRule, String subRuleId) {
        return new Touch90RuleDifference(careRule, 2, subRuleId, null);
    }

    static Touch90RuleDifference nodeChangeTime(TaskCareRule4Touch90Entity careRule, String subRuleId, LocalTime localTime) {
        return new Touch90RuleDifference(careRule, 3, subRuleId, localTime);
    }

    static Touch90RuleDifference ruleDisabled(TaskCareRule4Touch90Entity careRule) {
        return new Touch90RuleDifference(careRule, 4, true, null);
    }

    static Touch90RuleDifference ruleAdd(TaskCareRule4Touch90Entity careRule, boolean incloudCompany, Collection<CrmStoreEntity> stores) {
        return new Touch90RuleDifference(careRule, 10, incloudCompany, stores);
    }

    static Touch90RuleDifference ruleEdit(TaskCareRule4Touch90Entity careRule) {
        return new Touch90RuleDifference(careRule, 11, true, null);
    }

    static Touch90RuleDifference ruleEnabled(TaskCareRule4Touch90Entity careRule) {
        return new Touch90RuleDifference(careRule, 5, true, null);
    }

    static Touch90RuleDifference ruleRemove(TaskCareRule4Touch90Entity careRule) {
        return new Touch90RuleDifference(careRule, 8, true, null);
    }

    public String[] toJobParams() {
        return new String[]{"job.params", String.format("companyId=%s,storeId=%s,categories=%s", careRule.getCompanyId(),
                careRule.getStoreId(), careRule.getCategories())};
    }

    LocalTime getLocalTime() {
        return localTime;
    }

    public boolean isNodeRemove() {
        return this.action == 0;
    }

    public boolean isNodeAdd() {
        return this.action == 9;
    }

    public int getAction() {
        return action;
    }

    public BusinessType getBusinessType() {
        return careRule.getBusinessType();
    }

    public boolean isNodeDisabled() {
        return this.action == 1;
    }

    public boolean isNodeEnabled() {
        return this.action == 2;
    }

    public boolean isChangeTime() {
        return this.action == 3;
    }

    public boolean isRuleDisabled() {
        return this.action == 4;
    }

    public boolean isRuleEnabled() {
        return this.action == 5;
    }

    public boolean isCloseAuto() {
        return this.action == 6;
    }

    public boolean isOpenAuto() {
        return this.action == 7;
    }

    public boolean isRuleAdd() {
        return this.action == 10;
    }

    public boolean isRuleEdit() {
        return this.action == 11;
    }

    public Optional<List<CrmStoreEntity>> getStores() {
        return Optional.ofNullable(CollectionUtils.isEmpty(stores) ? null : stores);
    }

    public boolean ruleChange() {
        return isRuleAdd() || isRuleEdit();
    }

    public boolean isRuleRemove() {
        return this.action == 8;
    }

    public Integer getStoreId() {
        return careRule.getStoreId();
    }

    public Integer getCompanyId() {
        return careRule.getCompanyId();
    }

    public String getRuleId() {
        return careRule.getId();
    }

    public String getSubRuleId() {
        return subRuleId;
    }

    public TaskCareRule4Touch90Entity getCareRule() {
        return careRule;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", careRule.getCompanyId())
                .add("storeId", careRule.getStoreId())
                .add("businessType", careRule.getBusinessType())
                .add("ruleId", careRule.getId())
                .add("action", action)
                .add("subRuleId", subRuleId)
                .add("localTime", localTime)
                .toString();
    }
}
