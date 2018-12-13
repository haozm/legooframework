package com.legooframework.model.membercare.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UpcomingTaskDetailEntityAction extends BaseEntityAction<UpcomingTaskDetailEntity> {

    public UpcomingTaskDetailEntityAction() {
        super("CrmJobsCache");
    }

    public void batchInsert(List<UpcomingTaskDetailEntity> taskDetails) {
        if (CollectionUtils.isEmpty(taskDetails)) return;
        batchInsert("batchInsert", taskDetails);
    }

    public void batchUpdate(List<UpcomingTaskDetailEntity> details) {
        if (CollectionUtils.isEmpty(details)) return;
        super.batchUpdate("batchUpdate", (ps, detail) -> {
            ps.setObject(1, detail.getTaskStatus().getStatus());
            ps.setObject(2, detail.getFinishedDateTime().toDate());
            ps.setObject(3, detail.getId());
        }, details);
    }

    @Override
    protected RowMapper<UpcomingTaskDetailEntity> getRowMapper() {
        return null;
    }
}
