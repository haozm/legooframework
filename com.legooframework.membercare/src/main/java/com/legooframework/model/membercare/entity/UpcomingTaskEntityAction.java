package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UpcomingTaskEntityAction extends BaseEntityAction<UpcomingTaskEntity> {

    private static final Logger logger = LoggerFactory.getLogger(UpcomingTaskEntityAction.class);

    public UpcomingTaskEntityAction() {
        super(null);
    }

    @Override
    public Optional<UpcomingTaskEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("sql", "findById");
        Optional<UpcomingTaskEntity> task = super.queryForEntity("queryTasks", params, getRowMapper());
        if (!task.isPresent()) return Optional.empty();
        this.detailAction.initByTask(task.get());
        return task;
    }

    public Optional<List<UpcomingTaskEntity>> loadByIds(Collection<Integer> ids) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(ids), "带查询的taskIds 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        params.put("sql", "loadByIds");
        Optional<List<UpcomingTaskEntity>> tasksOpt = super.queryForEntities("queryTasks", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug("loadByIds(%s) size is %s", ids, tasksOpt.map(List::size).orElse(0));
        return tasksOpt;
    }

    public void saveOrUpdateTouch90Task(Collection<Touch90TaskDto> touch90TaskDto) {
        List<UpcomingTaskEntity> insert = Lists.newArrayList();
        List<UpcomingTaskEntity> update = Lists.newArrayList();
        List<UpcomingTaskDetailEntity> details = Lists.newArrayList();
        touch90TaskDto.forEach(x -> {
            if (CollectionUtils.isNotEmpty(x.getUpdates()))
                update.addAll(x.getUpdates());
            if (CollectionUtils.isNotEmpty(x.getInserts())) {
                insert.addAll(x.getInserts());
                x.getInserts().forEach(task -> details.addAll(task.getTaskDetails()));
            }
            if (CollectionUtils.isNotEmpty(x.getUpdateDetail()))
                details.addAll(x.getUpdateDetail());
        });

        if (CollectionUtils.isNotEmpty(insert)) super.batchInsert("batchInsertJob", insert);
        if (CollectionUtils.isNotEmpty(update)) {
            super.batchUpdate("batchUpdateJob", (ps, task) -> {
                ps.setObject(1, task.parseMergeInfo());
                ps.setObject(2, task.getId());
            }, update);
        }
        if (CollectionUtils.isNotEmpty(details)) super.batchInsert("saveOrUpdateJobDetail", details);
    }

    public Optional<List<UpcomingTaskEntity>> loadEnabledTouch90(CrmStoreEntity store, String categories) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", store.getCompanyId());
        params.put("businessType", BusinessType.TOUCHED90.toString());
        params.put("storeIds", Lists.newArrayList(store.getId()));
        if (!StringUtils.equals("0", categories)) params.put("categories", categories);
        params.put("sql", "loadEnabledTouch90Job");

        Optional<List<UpcomingTaskEntity>> tasks = super.queryForEntities("loadEnabledTouch90Job", params, getRowMapper());
        tasks.ifPresent(ts -> this.detailAction.initByTasks(ts));
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEnabledTouch90(%s,%s,%s) has size is %s", store.getCompanyId(),
                    store.getId(), tasks.map(List::size).orElse(0), categories));
        return tasks;
    }

    public Optional<Multimap<Integer, UpcomingTaskEntity>> loadEnabledTouch90ByStore(CrmOrganizationEntity company,
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
        // tasks.get().sort(TOUCH90_ORDERING);
        final Multimap<Integer, UpcomingTaskEntity> multimap = ArrayListMultimap.create();
        tasks.ifPresent(x -> x.forEach(task -> multimap.put(task.getMemberId(), task)));
        return Optional.of(multimap);
    }

    @Override
    protected RowMapper<UpcomingTaskEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private UpcomingTaskDetailEntityAction detailAction;

    public void setDetailAction(UpcomingTaskDetailEntityAction detailAction) {
        this.detailAction = detailAction;
    }

    class RowMapperImpl implements RowMapper<UpcomingTaskEntity> {
        @Override
        public UpcomingTaskEntity mapRow(ResultSet res, int i) throws SQLException {
            return new UpcomingTaskEntity(res.getInt("id"), res);
        }
    }

}
