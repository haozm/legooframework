package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskSwitchEntityAction extends BaseEntityAction<TaskSwitchEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TaskSwitchEntityAction.class);

    public TaskSwitchEntityAction() {
        super("CrmJobsCache");
    }

    public void updateTouch90Switch(final Integer storeId, boolean switched, LocalDateTime startDate) {
        LoginContext user = LoginContextHolder.get();
        Integer companyId = user.getTenantId().intValue();
        final String cache_key = String.format("%s_%s_taskswitch", getModelName(), BusinessType.TOUCHED90.getJobName());
        Optional<List<TaskSwitchEntity>> list = queryAllTouch90Switch();
        Preconditions.checkState(list.isPresent(), "尚未初始化公司%s的90 参数设置值...", user.getTenantId());
        Optional<TaskSwitchEntity> optional = list.get().stream().filter(x -> x.isBusinessType(BusinessType.TOUCHED90))
                .filter(x -> x.matched(companyId, storeId)).findFirst();
        Preconditions.checkState(optional.isPresent(), "待修改的开关量不存在....");
        Optional<TaskSwitchEntity> clone = optional.get().switched(user, switched, startDate);
        clone.ifPresent(x -> {
            super.updateAction("updateTouch90Switch", x.toParamMap());
            getCache().ifPresent(c -> c.evict(cache_key));
        });
    }

    /**
     * 停用指定门店的90服务
     *
     * @param store
     */
    public void closeTouch90Switch(final CrmStoreEntity store) {
        updateTouch90Switch(store.getId(), false, LocalDateTime.now());
    }

    public Optional<List<TaskSwitchEntity>> queryAllTouch90Switch() {
        Optional<List<TaskSwitchEntity>> list = queryTaskSwitch(BusinessType.TOUCHED90);
        if (!list.isPresent()) return Optional.empty();
        List<TaskSwitchEntity> sub_list = list.get().stream().filter(x -> x.isBusinessType(BusinessType.TOUCHED90))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    private Optional<List<TaskSwitchEntity>> queryTaskSwitch(BusinessType businessType) {
        final String cache_key = String.format("%s_%s_taskswitch", getModelName(), businessType.getJobName());
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<TaskSwitchEntity> cache = getCache().get().get(cache_key, List.class);
            if (cache != null) return Optional.of(cache);
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("businessType", businessType.toString());

        Optional<List<Integer>> companyIds = super.queryForList("SELECT company_id FROM TASK_JOB_SWITCH WHERE store_id = -1 AND business_type = :businessType",
                params, Integer.class);
        if (!companyIds.isPresent()) return Optional.empty();
        params.put("companyIds", companyIds.get());
        Optional<List<TaskSwitchEntity>> list = super.queryForEntities("queryTaskSwitch", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryTaskSwitch(%s) res size is %s", businessType, list.map(List::size).orElse(0)));
        getCache().ifPresent(c -> list.ifPresent(x -> c.put(cache_key, x)));
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
