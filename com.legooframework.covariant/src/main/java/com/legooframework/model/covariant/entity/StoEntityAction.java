package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoEntityAction extends BaseEntityAction<StoEntity> {

    public StoEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Override
    public StoEntity loadById(Object storeId) {
        Optional<StoEntity> exits = findById(storeId);
        Preconditions.checkState(exits.isPresent(), "Id=%s 对应的门店不存在...");
        return exits.get();
    }

    public Optional<StoEntity> findByOldInfo(String oldStoreId, String companypy) {
        final String cache_key = String.format("STO_OLD_%s_%s", oldStoreId.toLowerCase(), companypy.toLowerCase());
        if (getCache().isPresent()) {
            StoEntity store = getCache().get().get(cache_key, StoEntity.class);
            if (null != store) return Optional.of(store);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("oldStoreId", oldStoreId.toLowerCase());
        params.put("companypy", companypy.toLowerCase());
        params.put("sql", "findByOldInfo");
        Optional<StoEntity> store = super.queryForEntity("query4list", params, getRowMapper());
        getCache().ifPresent(c -> store.ifPresent(s -> c.put(cache_key, s)));
        return store;
    }

    public Optional<List<StoEntity>> findByIds(Collection<Integer> storeIds) {
        if (CollectionUtils.isEmpty(storeIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeIds", storeIds);
        params.put("sql", "findByIds");
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    public Optional<List<StoEntity>> findByIds(Integer... storeIds) {
        if (ArrayUtils.isEmpty(storeIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeIds", storeIds);
        params.put("sql", "findByIds");
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    @Override
    public Optional<StoEntity> findById(Object storeId) {
        Preconditions.checkArgument(storeId != null, "非法的入参 storeId = %s", storeId);
        final String cache_key = String.format("STO_BYID_%s", storeId);
        Optional<StoEntity> optional = Optional.empty();
        if (getCache().isPresent()) {
            StoEntity store = getCache().get().get(cache_key, StoEntity.class);
            optional = Optional.ofNullable(store);
        }
        if (optional.isPresent()) return optional;
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", storeId);
        params.put("sql", "findById");
        Optional<StoEntity> store = super.queryForEntity("query4list", params, getRowMapper());
        getCache().ifPresent(c -> store.ifPresent(s -> c.put(cache_key, s)));
        return store;
    }

    @Override
    protected RowMapper<StoEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<StoEntity> {
        @Override
        public StoEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new StoEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
