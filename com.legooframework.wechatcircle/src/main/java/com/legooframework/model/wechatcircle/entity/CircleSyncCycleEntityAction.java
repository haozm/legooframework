package com.legooframework.model.wechatcircle.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CircleSyncCycleEntityAction extends BaseEntityAction<CircleSyncCycleEntity> {

    public CircleSyncCycleEntityAction() {
        super("CirclePermissionCache");
    }

    @Override
    public Optional<CircleSyncCycleEntity> findById(Object id) {
        throw new UnsupportedOperationException("不再支持该方法调用");
    }

    public void update(CircleSyncCycleEntity syncCycle) {
        Optional<CircleSyncCycleEntity> exits = findById(syncCycle.getId(), syncCycle.getSyncType());
        if (exits.isPresent()) {
            exits.get().changeLastTime(syncCycle).ifPresent(x -> {
                super.updateAction(x, "updateLastTime");
                getCache().ifPresent(c -> c.evict(String.format("%s_%s_%s", getModelName(), x.getId(), x.getSyncType())));
            });
        } else {
            super.updateAction(syncCycle, "insert");
        }
    }

    public Optional<List<CircleSyncCycleEntity>> findByBatchWxIds(List<String> weixinIds) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("weixinIds", weixinIds.stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList()));
        params.put("syncType", 1);
        params.put("sql", "findByBatchWxIds");
        return super.queryForEntities("findByBatchWxIds", params, getRowMapper());
    }

    public Optional<CircleSyncCycleEntity> findById(String wxId, int syncType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", wxId);
        params.put("syncType", syncType);
        Optional<CircleSyncCycleEntity> opt = super.queryForEntity("findById", params, getRowMapper());
        getCache().ifPresent(c -> opt.ifPresent(o -> c.put(String.format("%s_%s_%s", getModelName(), wxId, syncType), o)));
        return opt;
    }

    @Override
    protected RowMapper<CircleSyncCycleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CircleSyncCycleEntity> {
        @Override
        public CircleSyncCycleEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new CircleSyncCycleEntity(res.getString("id"), res);
        }
    }
}
