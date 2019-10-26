package com.legooframework.model.autotask.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.OrgEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskSwitchEntityAction extends BaseEntityAction<TaskSwitchEntity> {
    private static final Logger logger = LoggerFactory.getLogger(TaskSwitchEntityAction.class);

    public TaskSwitchEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public void enabled(Integer id) {
        Optional<List<TaskSwitchEntity>> all_list = findAll();
        Preconditions.checkState(all_list.isPresent(), "id=%d 对应的设置不存在", id);
        Optional<TaskSwitchEntity> exits = all_list.get().stream().filter(x -> Objects.equals(x.getId(), id))
                .findFirst();
        Preconditions.checkState(exits.isPresent(), "id=%d 对应的设置不存在", id);
        if (exits.get().isEnabled()) return;
        Objects.requireNonNull(getJdbcTemplate()).update("UPDATE TASK_PWOER_SWITCH SET enabled = 1 WHERE id = ?", id);
        if (logger.isDebugEnabled())
            logger.debug(String.format("enabled(%s) is finished", id));
        getCache().ifPresent(c -> c.evict("TASK_SWITCH_ALL"));
    }

    public void disabled(Integer id) {
        Optional<List<TaskSwitchEntity>> all_list = findAll();
        Preconditions.checkState(all_list.isPresent(), "id=%d 对应的设置不存在", id);
        Optional<TaskSwitchEntity> exits = all_list.get().stream().filter(x -> Objects.equals(x.getId(), id))
                .findFirst();
        Preconditions.checkState(exits.isPresent(), "id=%d 对应的设置不存在", id);
        if (!exits.get().isEnabled()) return;
        Objects.requireNonNull(getJdbcTemplate()).update("UPDATE TASK_PWOER_SWITCH SET enabled = 0 WHERE id = ?", id);
        if (logger.isDebugEnabled())
            logger.debug(String.format("enabled(%s) is finished", id));
        getCache().ifPresent(c -> c.evict("TASK_SWITCH_ALL"));
    }

    public void add(OrgEntity company, BusinessType businessType) {
        TaskSwitchEntity switchs = new TaskSwitchEntity(company, businessType);
        Optional<List<TaskSwitchEntity>> all_list = findAll();
        if (all_list.isPresent()) {
            Optional<TaskSwitchEntity> exits = all_list.get().stream().filter(x -> x.hasSwitch(company, businessType))
                    .findFirst();
            if (exits.isPresent()) return;
        }
        super.batchInsert("batchInsert", Lists.newArrayList(switchs));
        if (logger.isDebugEnabled())
            logger.debug(String.format("add(%s) is finished", switchs));
        getCache().ifPresent(c -> c.evict("TASK_SWITCH_ALL"));
    }

    public Optional<List<TaskSwitchEntity>> findSwitchesOn() {
        Optional<List<TaskSwitchEntity>> switchs = findAll();
        if (!switchs.isPresent()) return Optional.empty();
        List<TaskSwitchEntity> switchs_on = switchs.get().stream().filter(TaskSwitchEntity::isEnabled)
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(switchs_on) ? null : switchs_on);
    }

    @SuppressWarnings("unchecked")
    private Optional<List<TaskSwitchEntity>> findAll() {
        final String cache_key = "TASK_SWITCH_ALL";
        if (getCache().isPresent()) {
            Object value = getCache().get().get(cache_key, Object.class);
            if (value != null) return Optional.of((List<TaskSwitchEntity>) value);
        }
        Optional<List<TaskSwitchEntity>> taskSwitches = super.queryForEntities("findAll", null, getRowMapper());
        getCache().ifPresent(c -> taskSwitches.ifPresent(t -> c.put(cache_key, t)));
        return taskSwitches;
    }

    @Override
    protected RowMapper<TaskSwitchEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<TaskSwitchEntity> {
        @Override
        public TaskSwitchEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TaskSwitchEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
