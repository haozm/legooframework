package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
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

public class UpcomingTaskEntityAction extends BaseEntityAction<UpcomingTaskEntity> {

    private static final Logger logger = LoggerFactory.getLogger(UpcomingTaskEntityAction.class);

    public UpcomingTaskEntityAction() {
        super("CrmJobsCache");
    }

    public void batchInsert(List<UpcomingTaskEntity> upcomingTasks) {
        if (CollectionUtils.isEmpty(upcomingTasks)) return;
        batchInsert("batchInsert", upcomingTasks);
    }

    public void batchUpdate(List<UpcomingTaskEntity> upcomingTasks) {
        if (CollectionUtils.isEmpty(upcomingTasks)) return;
        super.batchUpdate("batchUpdate", (ps, task) -> {
            ps.setObject(1, task.getTaskStatus().getStatus());
            ps.setObject(2, task.getId());
        }, upcomingTasks);
    }


    public Optional<List<UpcomingTaskEntity>> loadEnabledTouch90(CrmOrganizationEntity company,
                                                                 Collection<CrmStoreEntity> stores) {
        Preconditions.checkNotNull(company);
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        if (CollectionUtils.isNotEmpty(stores)) {
            params.put("storeIds", stores.stream().map(CrmStoreEntity::getId).collect(Collectors.toList()));
        }
        Optional<List<UpcomingTaskEntity>> tasks = queryForEntities("loadEnabledTouch90", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEnabledTouch90(%s,store's size:%s) has size is %s", company.getId(),
                    stores.size(), tasks.map(List::size).orElse(0)));
        return tasks;
    }

    public Optional<ArrayListMultimap<Integer, UpcomingTaskEntity>> loadEnabledTouch90(CrmOrganizationEntity company,
                                                                                       CrmStoreEntity store) {
        Preconditions.checkNotNull(company);
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        params.put("storeIds", Lists.newArrayList(store.getId()));
        Optional<List<UpcomingTaskEntity>> tasks = queryForEntities("loadEnabledTouch90", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEnabledTouch90(%s,store:%s) size is %s", company.getId(), store.getId(),
                    tasks.map(List::size).orElse(0)));
        if (!tasks.isPresent()) return Optional.empty();
        final ArrayListMultimap<Integer, UpcomingTaskEntity> multimap = ArrayListMultimap.create();
        tasks.ifPresent(x -> x.forEach(task -> {
            multimap.put(task.getMemberId(), task);
        }));
        return Optional.of(multimap);
    }


    @Override
    protected RowMapper<UpcomingTaskEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<UpcomingTaskEntity> {
        @Override
        public UpcomingTaskEntity mapRow(ResultSet res, int i) throws SQLException {
            TaskStatus status = TaskStatus.paras(res.getInt("taskStatus"));
            return new UpcomingTaskEntity(res.getString("id"), res, status);
        }
    }
}
