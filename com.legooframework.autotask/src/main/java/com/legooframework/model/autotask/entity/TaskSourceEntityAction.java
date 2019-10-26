package com.legooframework.model.autotask.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskSourceEntityAction extends BaseEntityAction<TaskSourceEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskSourceEntityAction.class);

    public TaskSourceEntityAction() {
        super(null);
    }

    public long queryTask4TodoCount(Collection<TaskSwitchEntity> taskSwitchs) {
        if (CollectionUtils.isEmpty(taskSwitchs)) return 0L;
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "findUndoCount");
        List<String> switches = taskSwitchs.stream().map(TaskSwitchEntity::toSqlString).collect(Collectors.toList());
        params.put("switches", switches);
        Optional<Long> count = super.queryForSimpleObj("query4Count", params, Long.class);
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryTask4TodoCount() return is %d", count.orElse(0L)));
        return count.orElse(0L);
    }

    public void updateStatus(Collection<? extends TaskSourceEntity> taskSources) {
        if (CollectionUtils.isEmpty(taskSources)) return;
        super.batchUpdate("updateStatus", (ps, o) -> {
            ps.setObject(1, o.getStatus());
            ps.setObject(2, o.getMessage());
            ps.setObject(3, o.getId());
        }, taskSources);
        if (logger.isDebugEnabled())
            logger.debug(String.format("本批次共计更新 TaskSourceEntity 状态 %s 条", taskSources.size()));
    }

    @Override
    protected RowMapper<TaskSourceEntity> getRowMapper() {
        return new RowMapperImpl();
    }


    public static class RowMapperImpl implements RowMapper<TaskSourceEntity> {
        @Override
        public TaskSourceEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TaskSourceEntity(resultSet.getLong("id"), resultSet);
        }
    }
}
