package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskSwitchEntity extends BaseEntity<Long> {

    private final Integer companyId;
    private final Integer storeId;
    private final TaskType taskType;
    private boolean enabled;

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
