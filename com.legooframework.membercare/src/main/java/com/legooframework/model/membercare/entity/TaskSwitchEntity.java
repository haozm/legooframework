package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class TaskSwitchEntity extends BaseEntity<Long> {

    private final Integer companyId;
    private final Integer storeId;
    private final TaskType taskType;
    private boolean enabled;

    static TaskSwitchEntity touc90Switch(CrmOrganizationEntity company, boolean enabled) {
        return new TaskSwitchEntity(company.getId(), -1, TaskType.Touche90, enabled, company.getId().longValue());
    }

    private TaskSwitchEntity(Integer companyId, Integer storeId, TaskType taskType, boolean enabled, Long tenantId) {
        super(0L, tenantId, 0L);
        this.companyId = companyId;
        this.storeId = storeId;
        this.taskType = taskType;
        this.enabled = enabled;
    }

    TaskSwitchEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.taskType = TaskType.parse(ResultSetUtil.getObject(res, "taskType", Integer.class));
            this.enabled = ResultSetUtil.getObject(res, "enabled", Integer.class) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Restore UpcomingTaskEntity has SQLException", e);
        }
    }

    Optional<TaskSwitchEntity> open() {
        if (enabled) return Optional.empty();
        TaskSwitchEntity clone = (TaskSwitchEntity) cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    Optional<TaskSwitchEntity> close() {
        if (!enabled) return Optional.empty();
        TaskSwitchEntity clone = (TaskSwitchEntity) cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("enabled", enabled);
        params.put("taskType", taskType.getValue());
        return params;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    boolean isBelongCompany(CrmOrganizationEntity company) {
        return Ints.compare(this.getCompanyId(), company.getId()) == 0;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("taskType", "enabled");
        params.put("taskType", taskType.getValue());
        params.put("enabled", enabled ? 1 : 0);
        return params;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("taskType", taskType)
                .add("enabled", enabled)
                .toString();
    }
}
