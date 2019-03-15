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
        LoginContext loginContext = LoginContextHolder.get();
        if (logger.isDebugEnabled())
            logger.debug(String.format("当前日志共计 %s 条需要批量写入....", list.size()));
        if (list.size() > 1024) {
            List<List<Touch90CareLogEntity>> sub_list = Lists.partition(list, 1024);
            sub_list.forEach(x -> runAsync(loginContext, x));
        } else {
            runAsync(loginContext, list);
        }
    }

    private void runAsync(LoginContext loginContext, List<Touch90CareLogEntity> list) {
        CompletableFuture.runAsync(() -> {
            LoginContextHolder.setCtx(loginContext);
            Optional<List<Touch90CareLogEntity>> exits = loadByInstances(list);
            exits.ifPresent(logs -> logs.forEach($cur -> {
                Optional<Touch90CareLogEntity> _exits = list.stream()
                        .filter($it -> $it.equalsInstance($cur)).findFirst();
                _exits.ifPresent(c -> c.merge($cur));
            }));
            super.batchInsert("batchInsert", list);
        }, getExecutorService());
    }

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
