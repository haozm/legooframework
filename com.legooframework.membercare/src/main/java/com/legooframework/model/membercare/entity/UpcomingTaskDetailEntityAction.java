package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpcomingTaskDetailEntityAction extends BaseEntityAction<UpcomingTaskDetailEntity> {

    private static final Logger logger = LoggerFactory.getLogger(UpcomingTaskDetailEntityAction.class);

    public UpcomingTaskDetailEntityAction() {
        super(null);
    }

    void initByTasks(Collection<UpcomingTaskEntity> tasks) {
        if (CollectionUtils.isEmpty(tasks)) return;
        List<Integer> taskIds = tasks.stream().map(BaseEntity::getId).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskIds", taskIds);
        params.put("sql", "loadByTasks");
        Optional<List<UpcomingTaskDetailEntity>> details = super.queryForEntities("query4Details", params, getRowMapper());
        Preconditions.checkState(details.isPresent(), "数据异常.... 任务对应的执行明细缺失");
        ArrayListMultimap<Integer, UpcomingTaskDetailEntity> multimap = ArrayListMultimap.create();
        details.get().forEach(x -> multimap.put(x.getTaskId(), x));
        tasks.forEach(x -> x.setTaskDetails(new UpcomingTaskDetails(multimap.get(x.getId()))));
    }

    void initByTask(UpcomingTaskEntity task) {
        Preconditions.checkNotNull(task, "待获取明细的任务不可以为空...");
        Optional<List<UpcomingTaskDetailEntity>> details = loadByTaskId(task.getId());
        Preconditions.checkState(details.isPresent(), "任务(taskType = %s ,id =%s ) 对应的任务明细不存在...", task.getBusinessType(),
                task.getId());
        task.setTaskDetails(new UpcomingTaskDetails(details.get()));
        if (logger.isDebugEnabled())
            logger.debug(String.format("initByTask(%s) list is %s", task.getId(), task.getTaskDetails().size()));
    }

    public UpcomingTaskDetails loadByTask(UpcomingTaskEntity task) {
        Optional<List<UpcomingTaskDetailEntity>> list = loadByTaskId(task.getId());
        Preconditions.checkState(list.isPresent(), "任务 taskId =%s 对应的任务明细不存在....");
        return new UpcomingTaskDetails(list.get());
    }

    Optional<List<UpcomingTaskDetailEntity>> loadByTaskId(Integer taskId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskId", taskId);
        params.put("sql", "loadByTask");
        Optional<List<UpcomingTaskDetailEntity>> details = super.queryForEntities("query4Details", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByTask(%s) list is %s", taskId, details.orElse(null)));
        return details;
    }

    /**
     * 获取一批数据 指定 detailIds 的  火眼进京
     *
     * @param detailIds 节点ID明细
     * @return shayemeiyou
     */
    public Optional<List<UpcomingTaskDetailEntity>> loadByDetailIds4Exec(Collection<Integer> detailIds) {
        Optional<List<UpcomingTaskDetailEntity>> details = loadByDetailIds(detailIds);
        if (details.isPresent()) {
            List<UpcomingTaskDetailEntity> exec_list = details.get().stream().filter(TaskStatusSupportEntity::canExec)
                    .collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(exec_list) ? null : exec_list);
        }
        return Optional.empty();
    }

    /**
     * 获取一批数据 指定 detailIds 的  火眼进京
     *
     * @param detailIds 节点ID明细
     * @return 啥也没有
     */
    public Optional<List<UpcomingTaskDetailEntity>> loadByDetailIds(Collection<Integer> detailIds) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(detailIds), "待查询明细的任务明细ID不可为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("detailIds", detailIds);
        params.put("sql", "loadByDetailIds");
        Optional<List<UpcomingTaskDetailEntity>> details = super.queryForEntities("query4Details", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByDetailIds(...) list is %s", details.map(List::size).orElse(0)));
        return details;
    }


    /**
     * 加载一批子任务 从创建 到 执行中
     *
     * @return
     */
    public Optional<List<UpcomingTaskDetailEntity>> loadDetails4Init(BusinessType businessType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("businessType", businessType.toString());
        params.put("taskStatus", TaskStatus.Create.getStatus());
        if (BusinessType.TOUCHED90 == businessType) {
            params.put("sql", "loadTouch90Detail4Init");
        }
        Optional<List<UpcomingTaskDetailEntity>> list = queryForEntities("query4Details", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadInitDetails (%s) size is %s .", businessType, list.map(List::size).orElse(0)));
        return list;
    }

    /**
     * 加载一批子任务 从创建 到 执行中
     *
     * @return 茄子
     */
    Optional<List<UpcomingTaskDetailEntity>> loadDetails4Extensioned(BusinessType businessType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("businessType", businessType.toString());
        params.put("taskStatus", TaskStatus.Starting.getStatus());
        if (BusinessType.TOUCHED90 == businessType) {
            params.put("sql", "loadDetails4Extensioned");
        }
        Optional<List<UpcomingTaskDetailEntity>> list = queryForEntities("query4Details", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadDetails4Extensioned (%s) size is %s .", businessType, list.map(List::size).orElse(0)));
        return list;
    }

    public void extensionedDetails() {
        Optional<List<UpcomingTaskDetailEntity>> init_list = this.loadDetails4Extensioned(BusinessType.TOUCHED90);
        List<UpcomingTaskDetailEntity> _list_4_start = Lists.newArrayList();
        init_list.ifPresent(x -> x.forEach(m -> m.makeExtensioned().ifPresent(_list_4_start::add)));
        if (CollectionUtils.isEmpty(_list_4_start)) return;
        super.batchUpdateBySql("UPDATE TASK_JOB_DETAIL SET task_status = 7 WHERE id = ?",
                (ps, t) -> ps.setObject(1, t.getId()), _list_4_start);
        if (logger.isDebugEnabled())
            logger.debug(String.format("extensionedDetails ( details size is %s) ", _list_4_start.size()));
    }

    /**
     * 创建 自动修改为 进行中
     */
    public void startDetails() {
        Optional<List<UpcomingTaskDetailEntity>> init_list = this.loadDetails4Init(BusinessType.TOUCHED90);
        if (!init_list.isPresent()) return;
        List<Integer> list_4_start = Lists.newArrayList();
        for (UpcomingTaskDetailEntity item : init_list.get()) {
            Optional<UpcomingTaskDetailEntity> clone = item.makeStarting();
            clone.ifPresent(x -> list_4_start.add(x.getId()));
        }
        if (CollectionUtils.isEmpty(list_4_start)) return;
        super.batchUpdateBySql("UPDATE TASK_JOB_DETAIL SET task_status = 2 WHERE id = ?", (ps, t) -> ps.setObject(1, t),
                list_4_start);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadDetails4Init ( details size is %s) ", list_4_start.size()));
    }

    public void finshedDetails(Collection<UpcomingTaskDetailEntity> taskDetails, final LoginContext user) {
        if (CollectionUtils.isEmpty(taskDetails)) return;
        List<UpcomingTaskDetailEntity> to_finsh_list = taskDetails.stream().filter(TaskStatusSupportEntity::canExec)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(to_finsh_list)) return;
        List<Integer> finish_list = to_finsh_list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        String update_sql = "UPDATE TASK_JOB_DETAIL SET task_status = 3, finished_date = NOW(), editor= ?, remarks ='线上完成' WHERE id = ?";
        if (user != null) {
            super.batchUpdateBySql(update_sql, (ps, t) -> {
                ps.setObject(1, user.getLoginId());
                ps.setObject(2, t);
            }, finish_list);
        } else {
            super.batchUpdateBySql(update_sql, (ps, t) -> {
                ps.setObject(1, -1);
                ps.setObject(2, t);
            }, finish_list);
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("finshedDetails ( details size is %s) ", to_finsh_list.size()));
    }

    public void finshedDetailsByOfferline(Collection<Integer> detailIds, String remarke, final Authenticationor author) {
        if (CollectionUtils.isEmpty(detailIds)) return;
        Optional<List<UpcomingTaskDetailEntity>> canceled_list_opt = this.loadByDetailIds4Exec(detailIds);
        if (!canceled_list_opt.isPresent()) return;
        List<UpcomingTaskDetailEntity> canceled_list = canceled_list_opt.get().stream().filter(x -> x.isCreated() || x.isStarting())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(canceled_list)) return;
        List<Integer> finish_list = canceled_list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        super.batchUpdateBySql("UPDATE TASK_JOB_DETAIL SET task_status = 3, finished_date = NOW(), remarks = ?, editor= ? WHERE id = ?",
                (ps, t) -> {
                    ps.setObject(1, remarke);
                    ps.setObject(2, author.getUser().getLoginId());
                    ps.setObject(3, t);
                }, finish_list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("finshedDetailsByOfferline ( details size is %s) ", canceled_list.size()));
    }

    public void canceledDetailIds(Collection<Integer> detailIds, String remarke) {
        LoginContext user = LoginContextHolder.get();
        if (CollectionUtils.isEmpty(detailIds)) return;
        Optional<List<UpcomingTaskDetailEntity>> canceled_list_opt = this.loadByDetailIds4Exec(detailIds);
        if (!canceled_list_opt.isPresent()) return;
        List<UpcomingTaskDetailEntity> canceled_list = canceled_list_opt.get().stream().filter(x -> x.isCreated() ||
                x.isStarting() || x.isExtensioned()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(canceled_list)) return;
        List<Integer> finish_list = canceled_list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        super.batchUpdateBySql("UPDATE TASK_JOB_DETAIL SET task_status = 5, remarks = ?, editor= ? WHERE id = ?", (ps, t) -> {
            ps.setObject(1, remarke);
            ps.setObject(2, user.getLoginId());
            ps.setObject(3, t);
        }, finish_list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("canceledDetails ( details size is %s) ", canceled_list.size()));
    }

    public void canceledDetails(Collection<UpcomingTaskDetailEntity> details, String remarke) {
        LoginContext user = LoginContextHolder.get();
        if (CollectionUtils.isEmpty(details)) return;
        List<UpcomingTaskDetailEntity> canceled_list = Lists.newArrayList();
        details.forEach(x -> x.makeCanceled().ifPresent(canceled_list::add));
        if (CollectionUtils.isEmpty(canceled_list)) return;
        List<Integer> finish_list = canceled_list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        super.batchUpdateBySql("UPDATE TASK_JOB_DETAIL SET task_status = 5, remarks = ?, editor= ? WHERE id = ?", (ps, t) -> {
            ps.setObject(1, remarke);
            ps.setObject(2, user.getLoginId());
            ps.setObject(3, t);
        }, finish_list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("canceledDetails ( details size is %s) ", canceled_list.size()));
    }

    /**
     * 加载需要标记为过期的子任务
     *
     * @return businessType 业务类型
     */
    Optional<List<UpcomingTaskDetailEntity>> loadDetails4Expired(BusinessType businessType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("businessType", businessType.toString());
        if (BusinessType.TOUCHED90 == businessType) {
            params.put("sql", "loadTouch90Detail4Expired");
        }
        Optional<List<UpcomingTaskDetailEntity>> list = queryForEntities("query4Details", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadDetails4Expired (%s) size is %s .", TaskStatus.Expired,
                    list.map(List::size).orElse(0)));
        return list;
    }

    /**
     * 加載可執行的SQL 用於自動化任務處理
     *
     * @param businessType BusinessType 任務分類
     * @return Optional 可有可無的存在
     */
    public Optional<List<UpcomingTaskDetailEntity>> loadAutoRunJobDetails(BusinessType businessType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("businessType", businessType.toString());
        params.put("sql", "loadAutoRunJobDetails");
        Optional<List<UpcomingTaskDetailEntity>> list = queryForEntities("query4Details", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAutoRunJobDetails (%s) size is %s .", businessType,
                    list.map(List::size).orElse(0)));
        return list;
    }

    /**
     * 批量更新 任务节点状态
     *
     * @param taskDetails 具体的节点明细
     */
    public void batchUpdateByEntity(Collection<UpcomingTaskDetailEntity> taskDetails) {
        if (CollectionUtils.isEmpty(taskDetails)) return;
        super.batchUpdateBySql("UPDATE TASK_JOB_DETAIL SET task_status = ?, remarks =? , finished_date = NOW() WHERE id = ?",
                (ps, task) -> {
                    ps.setObject(1, task.getTaskStatus().getStatus());
                    ps.setObject(2, task.getRemarks());
                    ps.setObject(3, task.getId());
                }, taskDetails);
    }

    /**
     * 创建 自动修改为 过期
     */
    public void expiredDetails() {
        Optional<List<UpcomingTaskDetailEntity>> start_list = this.loadDetails4Expired(BusinessType.TOUCHED90);
        if (!start_list.isPresent()) return;
        List<Integer> _list_4_expired = Lists.newArrayList();
        for (UpcomingTaskDetailEntity item : start_list.get()) {
            Optional<UpcomingTaskDetailEntity> clone = item.makeExpired();
            clone.ifPresent(x -> _list_4_expired.add(x.getId()));
        }
        if (CollectionUtils.isEmpty(_list_4_expired)) return;
        super.batchUpdateBySql("UPDATE TASK_JOB_DETAIL SET task_status = 6 WHERE id = ?",
                (ps, t) -> ps.setObject(1, t), _list_4_expired);
        if (logger.isDebugEnabled())
            logger.debug(String.format("expiredDetails ( details size is %s) ", _list_4_expired.size()));
    }

    public void execByEvent(List<Touch90RuleDifference> differences, int action) {
        if (CollectionUtils.isEmpty(differences)) return;
        switch (action) {
            case 8: // 规则删除
                super.batchUpdate("batchStopDetailsByRule", (ps, diff) -> {
                    // tenant_id = ? AND store_id = ? AND business_type = ? AND rule_id = ?
                    ps.setObject(1, diff.getCompanyId());
                    ps.setObject(2, diff.getBusinessType().toString());
                    ps.setObject(3, diff.getRuleId());
                }, differences);
                if (logger.isDebugEnabled())
                    logger.debug(String.format("batchStopDetailsByRule 停用规则子节点共计 %s ", differences.size()));
                break;
            case 3: // 变更开始时间
                super.batchUpdate("batchChangeAutoRunTimeBySubRule", (ps, diff) -> {
                    // tenant_id = ? AND store_id = ? AND business_type = ? AND rule_id = ?
                    ps.setObject(1, diff.getLocalTime().toString("HH:mm:ss"));
                    ps.setObject(2, diff.getCompanyId());
                    ps.setObject(3, diff.getBusinessType().toString());
                    ps.setObject(4, diff.getRuleId());
                    ps.setObject(5, diff.getSubRuleId());
                }, differences);
                if (logger.isDebugEnabled())
                    logger.debug(String.format("batchChangeAutoRunTimeBySubRule 修改规则子节点共计 %s ", differences.size()));
                break;
            case 6:
                // 取消自动发送
                super.batchUpdate("batchCancelAutoRunBySubRule", (ps, diff) -> {
                    // tenant_id = ? AND store_id = ? AND business_type = ? AND rule_id = ? AND sub_rule_id = ?
                    ps.setObject(1, diff.getCompanyId());
                    ps.setObject(2, diff.getBusinessType().toString());
                    ps.setObject(3, diff.getRuleId());
                    ps.setObject(4, diff.getSubRuleId());
                }, differences);
                if (logger.isDebugEnabled())
                    logger.debug(String.format("batchStopDetailsByRule 取消规则子节点共计 %s ", differences.size()));
                break;
            case 5: // 规则启用
                break;
            case 4: // 规则禁用
                break;
            case 0: // 子节点删除
                super.batchUpdate("batchStopDetailsBySubRule", (ps, diff) -> {
                    // tenant_id = ? AND store_id = ? AND business_type = ? AND rule_id = ? AND sub_rule_id = ?
                    ps.setObject(1, diff.getCompanyId());
                    ps.setObject(2, diff.getBusinessType().toString());
                    ps.setObject(3, diff.getRuleId());
                    ps.setObject(4, diff.getSubRuleId());
                }, differences);
                if (logger.isDebugEnabled())
                    logger.debug(String.format("batchStopDetailsBySubRule 禁用规则子节点共计 %s ", differences.size()));
                break;
            default:
                break;
        }
    }

    @Override
    protected RowMapper<UpcomingTaskDetailEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<UpcomingTaskDetailEntity> {
        @Override
        public UpcomingTaskDetailEntity mapRow(ResultSet res, int i) throws SQLException {
            return new UpcomingTaskDetailEntity(res.getInt("id"), res);
        }
    }

}
