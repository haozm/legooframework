package com.legooframework.model.takecare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CareNinetyEntityAction extends BaseEntityAction<CareNinetyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CareNinetyEntityAction.class);

    private static Comparator<CareNinetyTaskEntity> TASK_ORDER = Comparator.comparingInt(CareNinetyTaskEntity::getTaskNode);

    public CareNinetyEntityAction() {
        super(null);
    }

    public Optional<List<CareNinetyTaskEntity>> findTaskByIds(Collection<Integer> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskIds", taskIds);
        params.put("sql", "findByTaskIds");
        return queryForEntities("query4TaskList", params, new DetailRowMapperImpl());
    }

    @Override
    public Optional<CareNinetyEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("sql", "findById");
        Optional<List<CareNinetyEntity>> list = queryByParams(params);
        Optional<CareNinetyEntity> careNinety = list.map(x -> x.get(0));
        careNinety.ifPresent(this::initDetails);
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%s) return %s", id, careNinety.orElse(null)));
        return careNinety;
    }

    public CareNinetyTaskEntity processTask(Integer taskId, boolean canceled) {
        Optional<CareNinetyEntity> careNinety = findByTaskId(taskId);
        Preconditions.checkState(careNinety.isPresent(), "taskId=%s 对应的90主任务不存在....", taskId);
        CareNinetyTaskEntity current_task = careNinety.get().loadByTaskId(taskId);
        Optional<CareNinetyTaskEntity> change_task;
        if (canceled) {
            change_task = current_task.canceled();
        } else {
            change_task = current_task.finished();
        }
        change_task.ifPresent(this::updateTask);
//        if (change_task.isPresent()) {
//            Optional<CareNinetyTaskEntity> nextTask = careNinety.get().nextTask(change_task.get());
//            if (nextTask.isPresent()) {
//                careNinety.get().updateNextTask(nextTask.get());
//            } else {
//                careNinety.get().finished();
//            }
//            updateTask(change_task.get());
//            if (careNinety.get().isFinished()) {
//                finishCare(careNinety.get());
//            } else {
//                updateCare(careNinety.get());
//            }
//        }
        return change_task.orElse(current_task);
    }

//    private void updateCare(CareNinetyEntity care) {
//        String updateSql = "UPDATE acp.crm_ninetyplanfollowup SET planState=?, planNode=?, planPerformTime=?, updateTime=NOW() WHERE id=?";
//        Objects.requireNonNull(getJdbcTemplate()).update(updateSql, care.getPlanState(), care.getPlanNodeId(),
//                care.getPlanPerformTime().toDate(), care.getId());
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("[90] 主任务更新  %s 完成", care));
//    }

    private void updateTask(CareNinetyTaskEntity task) {
        String updateSql = "UPDATE acp.crm_plantask SET taskState= ?, doneTime=NOW(), remark= ? WHERE id = ?";
        Objects.requireNonNull(getJdbcTemplate()).update(updateSql, task.getTaskState(), task.getRemark(), task.getId());
        if (logger.isDebugEnabled())
            logger.debug(String.format("[90] 主任务 planId= %d ，更新子任务 %s", task.getPlanId(), task));
    }

    Optional<CareNinetyEntity> findByTaskId(Integer taskId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskId", taskId);
        params.put("sql", "findByTaskId");
        Optional<List<CareNinetyEntity>> list = queryByParams(params);
        Optional<CareNinetyEntity> careNinety = list.map(x -> x.get(0));
        careNinety.ifPresent(this::initDetails);
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByTaskId(%d) return %s", taskId, careNinety.orElse(null)));
        return careNinety;
    }

    private void initDetails(CareNinetyEntity careNinety) {
        Optional<List<CareNinetyTaskEntity>> details = queryTasks(careNinety);
        if (!details.isPresent()) return;
        details.get().sort(TASK_ORDER);
        careNinety.setDetails(details.get());
        // 处理OOXX数据
//        if (careNinety.isCanceled() || careNinety.isFinished()) {
//            List<CareNinetyTaskEntity> _cacales = Lists.newArrayList();
//            details.get().forEach(x -> x.canceled().ifPresent(_cacales::add));
//            if (CollectionUtils.isNotEmpty(_cacales)) {
//                cancelTasks(careNinety, _cacales);
//            }
//        }
//        Optional<CareNinetyTaskEntity> process = details.get().stream().filter(CareNinetyTaskEntity::isProcessing)
//                .findFirst();
//        if (!process.isPresent()) {
//            Optional<CareNinetyEntity> _ooxx = careNinety.finished();
//            _ooxx.ifPresent(this::finishCare);
//        }
    }

    private void cancelTasks(CareNinetyEntity care, List<CareNinetyTaskEntity> tasks) {
        if (CollectionUtils.isEmpty(tasks)) return;
        String updateSql = "UPDATE acp.crm_plantask SET taskState= 5, doneTime=null, remark='该节点取消执行' WHERE planId = %d AND id IN ( %s )";
        String ids_str = StringUtils.join(tasks.stream().mapToInt(BaseEntity::getId).boxed().collect(Collectors.toList()), ',');
        updateSql = String.format(updateSql, care.getId(), ids_str);
        if (logger.isDebugEnabled())
            logger.debug(String.format("[90] 主任务 planId= %d 完成或者取消，存在子任务进行中的 强制取消 %s", care.getId(), ids_str));
        Objects.requireNonNull(getJdbcTemplate()).update(updateSql);
    }

//    private void finishCare(CareNinetyEntity care) {
//        Objects.requireNonNull(getJdbcTemplate())
//                .update("UPDATE acp.crm_ninetyplanfollowup SET planState=2,updateTime=NOW() WHERE id = ?", care.getId());
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("[90] 主任务 planId= %d 完成", care.getId()));
//    }

    private Optional<List<CareNinetyTaskEntity>> queryTasks(CareNinetyEntity careNinety) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("planId", careNinety.getId());
        params.put("sql", "findByCareNinety");
        return queryForEntities("query4TaskList", params, new DetailRowMapperImpl());
    }

    private Optional<List<CareNinetyEntity>> queryByParams(Map<String, Object> params) {
        return queryForEntities("query4List", params, getRowMapper());
    }

    @Override
    protected RowMapper<CareNinetyEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<CareNinetyEntity> {
        @Override
        public CareNinetyEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new CareNinetyEntity(resultSet.getInt("id"), resultSet);
        }
    }

    private static class DetailRowMapperImpl implements RowMapper<CareNinetyTaskEntity> {
        @Override
        public CareNinetyTaskEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new CareNinetyTaskEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
