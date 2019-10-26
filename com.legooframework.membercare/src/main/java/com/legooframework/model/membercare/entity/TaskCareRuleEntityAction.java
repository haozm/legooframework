package com.legooframework.model.membercare.entity;

import com.google.common.collect.Lists;
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
import java.util.stream.Stream;

public class TaskCareRuleEntityAction extends BaseEntityAction<TaskCareRuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskCareRuleEntityAction.class);

    public TaskCareRuleEntityAction() {
        super("CrmJobsCache");
    }

    @Deprecated
    @Override
    public Optional<TaskCareRuleEntity> findById(Object id) {
        throw new UnsupportedOperationException("不支持该方法调用...");
    }

    <T extends TaskCareRuleEntity> void insert(T taskCareRule) {
        super.batchInsert("batchInsert", Lists.newArrayList(taskCareRule));
    }

    <T extends TaskCareRuleEntity> void insertBeforeDelete(T taskCareRule) {
        super.updateAction("deleteById", taskCareRule.toParamMap());
        super.batchInsert("batchInsert", Lists.newArrayList(taskCareRule));
    }

    <T extends TaskCareRuleEntity> void batchInsert(Collection<T> taskCareRules) {
        super.batchInsert("batchInsert", taskCareRules);
    }

    <T extends TaskCareRuleEntity> void batchDisabledByIds(Collection<T> taskCareRules) {
        List<String> ids = taskCareRules.stream().map(x -> String.format("'%s'", x.getId())).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        super.updateAction("batchDeleteByIds", params);
    }

    <T extends TaskCareRuleEntity> void enabledOrDisabledByIds(boolean enabled, Collection<String> ids) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("enabled", enabled ? 1 : 0);
        params.put("ids", ids.stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList()));
        super.updateAction("enabledOrDisabledByIds", params);
    }

    <T extends TaskCareRuleEntity> void disabledByIds(Collection<String> ids) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("enabled", 0);
        params.put("ids", ids.stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList()));
        super.updateAction("enabledOrDisabledByIds", params);
    }

    /**
     * 加载全部可用的规则
     *
     * @return
     */
    Optional<List<Integer>> findCompanyIdsByBusinessType(BusinessType businessType) {
        String exec_sql = "SELECT DISTINCT company_id FROM TASK_JOB_RULE WHERE company_id != -1 AND delete_flag = 0 AND business_type = :businessType";
        Map<String, Object> params = Maps.newHashMap();
        params.put("businessType", businessType.toString());
        Optional<List<Integer>> companyIds = super.queryForList(exec_sql, params, Integer.class);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadCompanyIdsByBusinessType(%s) res is %s", businessType, companyIds.orElse(null)));
        return companyIds;
    }

    Optional<List<TaskCareRuleEntity>> loadTaskCareRules(final Integer companyId, BusinessType businessType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("businessType", businessType.toString());
        params.put("companyId", companyId);
        params.put("sql", "loadRulesBybusinessType");
        Optional<List<TaskCareRuleEntity>> taskCareRules = super.queryForEntities("loadRulesBybusinessType", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTaskCareRules(%s,%s) size is %s", companyId, businessType,
                    taskCareRules.map(List::size).orElse(0)));
        return taskCareRules;
    }

    @Override
    protected RowMapper<TaskCareRuleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<TaskCareRuleEntity> {
        @Override
        public TaskCareRuleEntity mapRow(ResultSet res, int i) throws SQLException {
            return new TaskCareRuleEntity(res.getString("id"), res);
        }

    }

    private static final Comparator<TaskCareDetailRule> TOUCH90_DETAIL_ORDERING = Comparator
            .comparingLong(x -> x == null ? 0 : x.getDelay().toHours());

    private static Comparator<UpcomingTaskEntity> TOUCH90_TASK_ORDERING = Comparator
            .comparingLong(o -> o.getSaleDate().toDateTime().getMillis());

    static void sort90Tasks(List<UpcomingTaskEntity> touch90Tasks) {
        touch90Tasks.sort(TOUCH90_TASK_ORDERING);
    }

    static String joinDetails(List<TaskCareDetailRule> details) {
        return StringUtils.join(details, "$");
    }

    static List<TaskCareDetailRule> parseDetail(String details) {
        final List<TaskCareDetailRule> ruleDetails = Lists.newArrayList();
        String[] ruleBuilderSpeces = StringUtils.split(details, "$");
        Stream.of(ruleBuilderSpeces).forEach(ruleBuilderSpece -> ruleDetails.add(TaskCareDetailRule.createTouch90Rule(ruleBuilderSpece)));
        ruleDetails.sort(TOUCH90_DETAIL_ORDERING);
        return ruleDetails;
    }
}
