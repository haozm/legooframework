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

public class TaskExecuteEntityAction extends BaseEntityAction<TaskExecuteEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteEntityAction.class);

    public TaskExecuteEntityAction() {
        super(null);
    }

    @Override
    public Optional<TaskExecuteEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "findById");
        params.put("id", id);
        Optional<List<TaskExecuteEntity>> undo_list = findByParams(params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%s) returun %s", id, undo_list.map(x -> x.get(0)).orElse(null)));
        return undo_list.map(x -> x.get(0));
    }

    Optional<List<TaskExecuteEntity>> findTaskExecute4Todo() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "findTaskExecute4Todo");
        Optional<List<TaskExecuteEntity>> undo_list = findByParams(params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("findTaskExecute4Todo() size is %d", undo_list.map(List::size).orElse(0)));
        return undo_list;
    }

    public Optional<List<TaskExecuteEntity>> findTaskExecute4Jobs() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "findTaskExecute4Jobs");
        Optional<List<TaskExecuteEntity>> undo_list = findByParams(params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("findTaskExecute4Jobs() size is %d", undo_list.map(List::size).orElse(0)));
        return undo_list;
    }

    public void batchUpdateStatus(Collection<? extends TaskExecuteEntity> taskExecutes) {
        if (CollectionUtils.isEmpty(taskExecutes)) return;
        super.batchUpdateBySql("UPDATE TASK_EXECUTE_LOG SET status = ?, message = ? WHERE id = ?",
                (ps, o) -> {
                    ps.setObject(1, o.getStatus());
                    ps.setObject(2, o.getMessage());
                    ps.setObject(3, o.getId());
                }, taskExecutes);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchUpdateStatus() size is %d", taskExecutes.size()));
    }

    private Optional<List<TaskExecuteEntity>> findByParams(Map<String, Object> params) {
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    public void batchInsert(Collection<TaskExecuteEntity> taskExecutes) {
        if (CollectionUtils.isEmpty(taskExecutes)) return;
        super.batchInsert("batchInsert", taskExecutes);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(taskExecutes...) size is %s", taskExecutes.size()));
    }

    @Override
    protected RowMapper<TaskExecuteEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    public static class RowMapperImpl implements RowMapper<TaskExecuteEntity> {
        @Override
        public TaskExecuteEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TaskExecuteEntity(resultSet.getLong("id"), resultSet);
        }
    }
}
