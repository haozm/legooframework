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

    @SuppressWarnings("unchecked")
    public Optional<List<TaskSwitchEntity>> queryAllTouch90Switch() {
        final String cache_key = String.format("%s_%s_all", getModelName(), TaskType.Touche90.getValue());
        if (getCache().isPresent()) {
            List<TaskSwitchEntity> cache = getCache().get().get(cache_key, List.class);
            if (cache != null) return Optional.of(cache);
        }
        Optional<List<TaskSwitchEntity>> optlist = queryTaskSwitch(TaskType.Touche90.getValue());
        if (getCache().isPresent() && optlist.isPresent()) {
            getCache().get().put(cache_key, optlist.get());
        }
        return optlist;
    }

    public void updateTouch90Switch(CrmOrganizationEntity companyn, boolean switched) {
        Preconditions.checkNotNull(companyn, "入参 CrmOrganizationEntity company 公司不可以为空...");
        Optional<TaskSwitchEntity> taskSwitch = queryTouch90Switch(companyn);
        final String cache_key = String.format("%s_%s_all", getModelName(), TaskType.Touche90.getValue());
        if (taskSwitch.isPresent()) {
            Optional<TaskSwitchEntity> clone = switched ? taskSwitch.get().open() : taskSwitch.get().close();
            clone.ifPresent(x -> super.updateAction(x, "updateTouch90Switch"));
        } else {
            TaskSwitchEntity un_saved = TaskSwitchEntity.touc90Switch(companyn, switched);
            super.updateAction(un_saved, "insertTouch90Switch");
        }
        getCache().ifPresent(c -> c.evict(cache_key));
    }

    public Optional<TaskSwitchEntity> queryTouch90Switch(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参 CrmOrganizationEntity company 公司不可以为空...");
        Optional<List<TaskSwitchEntity>> all_opts = queryAllTouch90Switch();
        return all_opts.flatMap(swt -> swt.stream().filter(x -> x.isBelongCompany(company)).findFirst());
    }

    private Optional<List<TaskSwitchEntity>> queryTaskSwitch(Integer taskType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskType", taskType);
        Optional<List<TaskSwitchEntity>> list = super.queryForEntities("queryTaskSwitch", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryTaskSwitch( %s ) res size is %s", taskType,
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
