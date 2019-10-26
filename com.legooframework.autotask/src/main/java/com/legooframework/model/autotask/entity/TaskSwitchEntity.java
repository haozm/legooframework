package com.legooframework.model.autotask.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.OrgEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TaskSwitchEntity extends BaseEntity<Integer> implements BatchSetter {

    private final BusinessType businessType;
    private final int companyId;
    private boolean enabled;

    TaskSwitchEntity(OrgEntity company, BusinessType businessType) {
        super(0);
        this.companyId = company.getId();
        this.businessType = businessType;
        this.enabled = true;
    }

    boolean hasSwitch(OrgEntity company, BusinessType businessType) {
        return isCompany(company) && isBusinessType(businessType);
    }

    boolean isCompany(OrgEntity company) {
        return Objects.equals(this.companyId, company.getId());
    }

    boolean isBusinessType(BusinessType businessType) {
        return this.businessType == businessType;
    }

    TaskSwitchEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.businessType = BusinessType.paras(res.getInt("business_type"));
            this.companyId = ResultSetUtil.getObject(res, "company_id", Integer.class);
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
        } catch (SQLException e) {
            throw new RuntimeException("Restore TaskExecuteEntity has SQLException", e);
        }
    }

    boolean isEnabled() {
        return enabled;
    }

    public String toSqlString() {
        return String.format("%s,%s", this.companyId, this.businessType.getValue());
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, companyId);
        ps.setObject(2, businessType.getValue());
        ps.setObject(3, enabled ? 1 : 0);
        ps.setObject(4, companyId);
    }

    Optional<TaskSwitchEntity> enabled() {
        if (this.enabled) return Optional.empty();
        TaskSwitchEntity clone = (TaskSwitchEntity) this.cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    Optional<TaskSwitchEntity> disabled() {
        if (!this.enabled) return Optional.empty();
        TaskSwitchEntity clone = (TaskSwitchEntity) this.cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("switchId", getId());
        params.put("businessType", businessType.getValue());
        params.put("enabled", enabled ? 1 : 0);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskSwitchEntity that = (TaskSwitchEntity) o;
        return companyId == that.companyId &&
                businessType == that.businessType &&
                enabled == that.enabled;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("businessType", businessType)
                .add("enabled", enabled)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, businessType, enabled);
    }
}
