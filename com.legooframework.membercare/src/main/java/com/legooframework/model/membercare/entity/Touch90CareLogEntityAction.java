package com.legooframework.model.membercare.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Touch90CareLogEntityAction extends BaseEntityAction<Touch90CareLogEntity> {

    private static final Logger logger = LoggerFactory.getLogger(Touch90CareLogEntityAction.class);

    public Touch90CareLogEntityAction() {
        super("CrmJobsCache");
    }

    private Optional<List<Touch90CareLogEntity>> loadByInstances(List<Touch90CareLogEntity> touch90Logs) {
        if (CollectionUtils.isEmpty(touch90Logs)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        List<List<String>> instances = Lists.newArrayList();
        touch90Logs.forEach(x -> {
            List<String> item = Lists.newArrayList();
            item.add(x.getCompanyId().toString());
            item.add(x.getStoreId().toString());
            item.add(x.getLogDate().toString("yyyy-MM-dd"));
            instances.add(item);
        });
        params.put("instances", instances);
        return queryForEntities("loadByInstances", params, getRowMapper());
    }

    public void savaOrUpdate(List<Touch90CareLogEntity> list) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("当前日志共计 %s 条需要批量写入....", list.size()));
        super.batchInsert(BATCHINSERT_SQL, 512, list);
    }

    private static String BATCHINSERT_SQL = "REPLACE INTO TASK_TOUCH90_LOG " +
            "(company_id, store_id, log_date, add_list, add_size, update_list, update_size,tenant_id, categories, log_date_pk ) " +
            " VALUES ( ?, ?, ?, ?, ?, ?, ?, ? , ? ,? )";

    @Override
    protected RowMapper<Touch90CareLogEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<Touch90CareLogEntity> {
        @Override
        public Touch90CareLogEntity mapRow(ResultSet res, int i) throws SQLException {
            return new Touch90CareLogEntity("", res);
        }
    }

}
