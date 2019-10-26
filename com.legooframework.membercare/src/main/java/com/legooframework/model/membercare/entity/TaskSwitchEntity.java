package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TaskSwitchEntity extends BaseEntity<Long> {

    private final Integer companyId;
    private final Integer storeId;
    private final BusinessType businessType;
    private LocalDateTime startDate;
    private boolean enabled;

    static TaskSwitchEntity touc90Switch(LoginContext user, Integer storeId, boolean enabled, LocalDateTime startDate) {
        return new TaskSwitchEntity(user.getTenantId().intValue(), storeId, BusinessType.TOUCHED90, enabled, startDate);
    }

    Integer getStoreId() {
        return storeId;
    }

    private TaskSwitchEntity(Integer companyId, Integer storeId, BusinessType businessType, boolean enabled,
                             LocalDateTime startDate) {
        super(0L, companyId.longValue(), 0L);
        this.companyId = companyId;
        this.storeId = storeId;
        this.businessType = businessType;
        this.enabled = enabled;
        this.startDate = startDate;
    }

    TaskSwitchEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            Object store = res.getObject("storeId");
            this.storeId = (store instanceof Long) ? ResultSetUtil.getObject(res, "storeId", Long.class).intValue() :
                    ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.businessType = BusinessType.parse(ResultSetUtil.getString(res, "businessType"));
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
            this.startDate = LocalDateTime.fromDateFields(res.getTimestamp("startDate"));
        } catch (SQLException e) {
            throw new RuntimeException("Restore TaskSwitchEntity has SQLException", e);
        }
    }

    BusinessType getBusinessType() {
        return businessType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    boolean isBusinessType(BusinessType businessType) {
        return this.businessType == businessType;
    }

    Optional<TaskSwitchEntity> switched(LoginContext user, boolean enbaled, LocalDateTime startDate) {
        TaskSwitchEntity clone = (TaskSwitchEntity) cloneMe();
        clone.enabled = enbaled;
        if (null != startDate) clone.startDate = startDate;
        clone.setEditor(user.getLoginId());
        if (clone.equals(this)) return Optional.empty();
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("enabled", enabled);
        params.put("startDate", this.startDate.toString("yyyy-MM-dd HH:mm:ss"));
        params.put("businessType", businessType.toString());
        return params;
    }

    boolean matched(Integer companyId, Integer storeId) {
        return this.getCompanyId().equals(companyId) && this.storeId.equals(storeId);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean matchCompany(CrmOrganizationEntity company) {
        return this.companyId.equals(company.getId());
    }

    public boolean matchStore(CrmStoreEntity store) {
        return this.companyId.equals(store.getCompanyId()) && this.storeId.equals(store.getId());
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("businessType", "enabled");
        params.put("businessType", businessType.toString());
        params.put("enabled", enabled ? 1 : 0);
        params.put("startDate", startDate.toDate());
        params.put("storeId", this.storeId);
        params.put("companyId", this.companyId);
        return params;
    }

    public boolean isCompany() {
        return this.storeId == -1;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskSwitchEntity that = (TaskSwitchEntity) o;
        return enabled == that.enabled &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(storeId, that.storeId) &&
                businessType == that.businessType &&
                Objects.equals(startDate.toString("yyyyMMddHHmmss"), that.startDate.toString("yyyyMMddHHmmss"));
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, storeId, businessType, startDate.toString("yyyyMMddHHmmss"), enabled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("businessType", businessType)
                .add("enabled", enabled)
                .toString();
    }
}
