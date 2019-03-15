package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Ints;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class AbstractCareRuleRule extends BaseEntity<String> implements BatchSetter {

    private final TaskType taskType;
    private boolean automatic;
    private final Integer companyId;
    private Integer storeId;

    AbstractCareRuleRule(Long tenantId, Long creator, TaskType taskType, boolean automatic,
                         Integer storeId, Integer companyId) {
        super("NOID", tenantId, creator);
        this.taskType = taskType;
        this.automatic = automatic;
        this.storeId = storeId;
        this.companyId = companyId;
    }

    AbstractCareRuleRule(TaskType taskType, Integer companyId, Integer storeId) {
        super("NOID");
        this.taskType = taskType;
        this.companyId = companyId;
        this.storeId = storeId;
        this.automatic = false;
    }

    AbstractCareRuleRule(ResultSet res, TaskType taskType, boolean automatic, Integer storeId, Integer companyId) {
        super("NOID", res);
        this.taskType = taskType;
        this.automatic = automatic;
        this.storeId = storeId;
        this.companyId = companyId;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getCompanyId());
        ps.setObject(2, getStoreId() == null ? -1 : getStoreId());
        ps.setObject(3, taskType.getValue());
        ps.setObject(4, automatic ? 1 : 0);
        ps.setObject(5, getTenantId());
        ps.setObject(6, getCreator());
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


    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    boolean isBelongCompany(CrmOrganizationEntity company) {
        return Ints.compare(this.getCompanyId(), company.getId()) == 0;
    }

    void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    boolean isOnlyCompany() {
        return this.storeId == -1;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("storeId", storeId);
        params.put("companyId", companyId);
        params.put("taskType", taskType.getValue());
        params.put("automatic", automatic ? 1 : 0);
        return params;
    }

    boolean exitsRule(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCareRuleRule)) return false;
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
        return automatic == that.automatic &&
                taskType == that.taskType &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskType, automatic, storeId, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("RULE")
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("taskType", taskType)
                .add("automatic", automatic)
                .toString();
    }
}
