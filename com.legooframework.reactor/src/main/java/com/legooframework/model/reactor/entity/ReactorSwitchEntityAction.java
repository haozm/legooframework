package com.legooframework.model.reactor.entity;

import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ReactorSwitchEntityAction extends BaseEntityAction<ReactorSwitchEntity> {

    public ReactorSwitchEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public void eidtRetailFactSwitch(OrgEntity company, List<StoEntity> stores) {
        Set<Integer> storeIds = CollectionUtils.isEmpty(stores) ? null : stores.stream().mapToInt(StoEntity::getId).boxed()
                .collect(Collectors.toSet());
        Optional<List<ReactorSwitchEntity>> all_list = loadAll();
        Optional<ReactorSwitchEntity> com_switched = Optional.empty();
        if (all_list.isPresent()) {
            com_switched = all_list.get().stream().filter(x -> x.isCompany(company))
                    .filter(x -> x.isType(ReactorSwitchEntity.TYPE_RETAILFACT)).findFirst();
        }
        if (com_switched.isPresent()) {
            Optional<ReactorSwitchEntity> clone = com_switched.get().allowStoreIds(storeIds);
            clone.ifPresent(o -> super.updateAction(o, "updateWhitelist"));
        } else {
            ReactorSwitchEntity instance = ReactorSwitchEntity.retailFactSwitch(company, stores);
            super.updateAction(instance, "insert");
        }
        getCache().ifPresent(c -> c.evict("REACTOR_SWITCH_ALL"));
    }

    public Optional<ReactorSwitchEntity> findRetailFactSwitch(OrgEntity company) {
        Optional<List<ReactorSwitchEntity>> all_list = loadAll();
        return all_list.flatMap(rs -> rs.stream().filter(x -> x.isCompany(company))
                .filter(x -> x.isType(ReactorSwitchEntity.TYPE_RETAILFACT)).findFirst());
    }

    @SuppressWarnings("unchecked")
    Optional<List<ReactorSwitchEntity>> loadAll() {
        final String cache_key = "REACTOR_SWITCH_ALL";
        if (getCache().isPresent()) {
            Object cacheVal = getCache().get().get(cache_key, Object.class);
            if (null != cacheVal) return Optional.of((List<ReactorSwitchEntity>) cacheVal);
        }
        Optional<List<ReactorSwitchEntity>> list = super.queryForEntities("find4All", null, getRowMapper());
        getCache().ifPresent(c -> list.ifPresent(l -> c.put(cache_key, l)));
        return list;
    }

    @Override
    protected RowMapper<ReactorSwitchEntity> getRowMapper() {
        return new RowMapperImple();
    }

    private static class RowMapperImple implements RowMapper<ReactorSwitchEntity> {

        @Override
        public ReactorSwitchEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new ReactorSwitchEntity(res.getLong("id"), res);
        }

    }
}
