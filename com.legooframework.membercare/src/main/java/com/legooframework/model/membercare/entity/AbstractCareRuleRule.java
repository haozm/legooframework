package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Objects;

public class AbstractCareRuleRule extends BaseEntity<String> {

    private final TaskType taskType;
    private final boolean enabled;
    private final boolean automatic;
    private final Integer storeId, companyId;

    AbstractCareRuleRule(Long tenantId, Long creator, TaskType taskType, boolean enabled, boolean automatic,
                         Integer storeId, Integer companyId) {
        super("NOID", tenantId, creator);
        this.taskType = taskType;
        this.enabled = enabled;
        this.automatic = automatic;
        this.storeId = storeId;
        this.companyId = companyId;
    }

    AbstractCareRuleRule(ResultSet res, TaskType taskType, boolean enabled, boolean automatic,
                         Integer storeId, Integer companyId) {
        super("NOID", res);
        this.taskType = taskType;
        this.enabled = enabled;
        this.automatic = automatic;
        this.storeId = storeId;
        this.companyId = companyId;
    }

    boolean isAutomatic() {
        return automatic;
    }

    boolean isBrithDayJob() {
        return this.taskType == TaskType.BrithDay;
    }

    boolean isTouche90Job() {
        return this.taskType == TaskType.Touche90;
    }

    boolean isFestivalDayJob() {
        return this.taskType == TaskType.FestivalDay;
    }

    boolean isEnabled() {
        return enabled;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("storeId", storeId);
        params.put("companyId", companyId);
        params.put("taskType", taskType.getValue());
        params.put("enabled", enabled ? 1 : 0);
        params.put("automatic", automatic ? 1 : 0);
        return params;
    }

    public boolean equalsTaskType(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCareRuleRule that = (AbstractCareRuleRule) o;
        return taskType == that.taskType &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(companyId, that.companyId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCareRuleRule)) return false;
        AbstractCareRuleRule that = (AbstractCareRuleRule) o;
        return enabled == that.enabled &&
                automatic == that.automatic &&
                taskType == that.taskType &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskType, enabled, automatic, storeId, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("RULE")
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("taskType", taskType)
                .add("enabled", enabled)
                .add("automatic", automatic)
                .toString();
    }
}
