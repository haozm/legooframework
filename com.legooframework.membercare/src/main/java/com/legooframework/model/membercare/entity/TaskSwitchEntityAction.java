package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskSwitchEntityAction extends BaseEntityAction<TaskSwitchEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskSwitchEntityAction.class);

    public TaskSwitchEntityAction() {
        super("CrmJobsCache");
    }

    public Optional<TaskSwitchEntity> getTouch90Task(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "公司不可以为空...");
        final String cache_key = String.format("%s_%s_%s", getModelName(), TaskType.Touche90.getValue(), company.getId());
        if (getCache().isPresent()) {
            TaskSwitchEntity cache = getCache().get().get(cache_key, TaskSwitchEntity.class);
            if (cache != null) return Optional.of(cache);
        }
        Optional<List<TaskSwitchEntity>> list = queryForTask(company.getId(), -1, TaskType.Touche90.getValue());
        if (!list.isPresent()) return Optional.empty();
        TaskSwitchEntity entit = list.get().get(0);
        getCache().ifPresent(c -> c.put(cache_key, entit));
        return Optional.of(entit);
    }

    private Optional<List<TaskSwitchEntity>> queryForTask(Integer companyId, Integer storeId, Integer taskName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskId", taskName);
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        Optional<List<TaskSwitchEntity>> list = super.queryForEntities("findByTask", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryForTask(%s, %s, %s) res size is %s", companyId, storeId, taskName,
                    list.map(List::size).orElse(0)));
        return list;
    }


    @Override
    protected RowMapper<TaskSwitchEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<TaskSwitchEntity> {
        @Override
        public TaskSwitchEntity mapRow(ResultSet res, int i) throws SQLException {
            return new TaskSwitchEntity(res.getLong("id"), res);
        }
    }
}
