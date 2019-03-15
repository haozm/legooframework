package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UpcomingTaskEntityAction extends BaseEntityAction<UpcomingTaskEntity> {

    private static final Logger logger = LoggerFactory.getLogger(UpcomingTaskEntityAction.class);

    public UpcomingTaskEntityAction() {
        super("CrmJobsCache");
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

        List<CompletableFuture<int[][]>> cfs = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(insert)) {
            if (insert.size() > 2048) {
                List<List<UpcomingTaskEntity>> sub_list = Lists.partition(insert, 2048);
                sub_list.forEach(x -> cfs.add(super.asyncBatchInsert("batchInsert", x)));
            } else {
                cfs.add(super.asyncBatchInsert("batchInsert", insert));
            }
        }
        if (CollectionUtils.isNotEmpty(update)) {
            if (update.size() > 1024) {
                List<List<UpcomingTaskEntity>> sub_list = Lists.partition(update, 1024);
                sub_list.forEach(x ->
                        cfs.add(super.asyncBatchUpdate("batchUpdate", (ps, task) -> {
                            ps.setObject(1, task.getContext());
                            ps.setObject(2, task.getId());
                        }, x)));
            } else {
                cfs.add(super.asyncBatchUpdate("batchUpdate", (ps, task) -> {
                    ps.setObject(1, task.getContext());
                    ps.setObject(2, task.getId());
                }, update));
            }
        }
        CompletableFuture<Void> wait_all = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]))
                .whenComplete((v, th) -> {
                    if (logger.isDebugEnabled())
                        logger.debug(String.format("saveOrUpdateTouch90Task(2048, size is %s ) finshed", cfs.size()));
                });
        wait_all.join();

        cfs.clear();
        if (CollectionUtils.isNotEmpty(details)) {
            if (details.size() > 2048) {
                List<List<UpcomingTaskDetailEntity>> sub_list = Lists.partition(details, 2048);
                sub_list.forEach(x -> cfs.add(super.asyncBatchInsert("saveOrUpdateDetail", x)));
            } else {
                cfs.add(super.asyncBatchInsert("saveOrUpdateDetail", details));
            }
        }
        wait_all = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]))
                .whenComplete((v, th) -> {
                    if (logger.isDebugEnabled())
                        logger.debug(String.format("saveOrUpdateTouch90Task(2048, size is %s ) finshed", cfs.size()));
                });
        wait_all.join();
    }

    public Optional<List<UpcomingTaskDetailEntity>> loadTouch90DetailByStauts(TaskStatus taskStatus) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskType", TaskType.Touche90.getValue());
        params.put("taskStatus", taskStatus.getStatus());
        Optional<List<UpcomingTaskDetailEntity>> list = queryForEntities("loadDetailByStatus", params, new DetailRowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90DetailByStauts (%s) size is %s .", taskStatus,
                    list.map(List::size).orElse(0)));
        return list;
    }

    public void updateDetailToStart(List<UpcomingTaskDetailEntity> taskStatusSupports) {
        if (CollectionUtils.isEmpty(taskStatusSupports)) return;
        super.batchUpdate("updateDetailToStart", (ps, t) -> ps.setObject(1, t.getId()), taskStatusSupports);
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateDetailToStart ( taskStatusSupports size is %s) ", taskStatusSupports.size()));
    }

    public void updateDetailToExpired(List<UpcomingTaskDetailEntity> taskStatusSupports) {
        if (CollectionUtils.isEmpty(taskStatusSupports)) return;
        super.batchUpdate("updateDetailToExpired", (ps, t) -> ps.setObject(1, t.getId()), taskStatusSupports);
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateDetailToExpired ( taskStatusSupports size is %s) ", taskStatusSupports.size()));
    }

    public Optional<List<UpcomingTaskDetailEntity>> loadDetailById(String taskId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(taskId), "任务ID不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", taskId);
        Optional<UpcomingTaskEntity> tasks = queryForEntity("findById", params, new RowMapperImpl());
        return tasks.map(UpcomingTaskEntity::getTaskDetails);
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
        // tasks.ifPresent(x -> x.sort(TOUCH90_ORDERING));
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
        // tasks.get().sort(TOUCH90_ORDERING);
        final ArrayListMultimap<Integer, UpcomingTaskEntity> multimap = ArrayListMultimap.create();
        tasks.ifPresent(x -> x.forEach(task -> multimap.put(task.getMemberId(), task)));
        return Optional.of(multimap);
    }

    @Override
    protected RowMapper<UpcomingTaskEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<UpcomingTaskEntity> {
        @Override
        public UpcomingTaskEntity mapRow(ResultSet res, int i) throws SQLException {
            return new UpcomingTaskEntity(res.getString("id"), res);
        }
    }

    class DetailRowMapperImpl implements RowMapper<UpcomingTaskDetailEntity> {
        @Override
        public UpcomingTaskDetailEntity mapRow(ResultSet res, int i) throws SQLException {
            return new UpcomingTaskDetailEntity(res.getString("id"), res);
        }
    }

}
