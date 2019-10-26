package com.legooframework.model.covariant.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
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
import java.util.stream.Collectors;

public class EmpEntityAction extends BaseEntityAction<EmpEntity> {

    private static final Logger logger = LoggerFactory.getLogger(EmpEntityAction.class);

    public EmpEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public Optional<List<EmpEntity>> findManagersByOrg(OrgEntity org) {
        Optional<List<EmpEntity>> emps = findEmpsByOrg(org);
        if (logger.isDebugEnabled())
            logger.debug(String.format("findManagersByOrg(%s) return %s", emps.map(List::size).orElse(0)));
        return emps;
    }

    public Optional<List<EmpEntity>> findStoreManagersByStore(StoEntity store) {
        Optional<List<EmpEntity>> emps = findEmpsByStore(store);
        if (emps.isPresent()) {
            List<EmpEntity> list = emps.get().stream().filter(EmpEntity::isStoreManager).collect(Collectors.toList());
            if (logger.isDebugEnabled())
                logger.debug(String.format("findStoreManagersByStore(%s) return %s", store.getId(), list));
            return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list);
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("findStoreManagersByStore(%s) return empty", store.getId()));
        return Optional.empty();
    }

    @Override
    public Optional<EmpEntity> findById(Object id) {
        final String cache_key = String.format("EMP_BYID_%s", id);
        if (getCache().isPresent()) {
            EmpEntity cache_val = getCache().get().get(cache_key, EmpEntity.class);
            if (cache_val != null) return Optional.of(cache_val);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("empId", id);
        params.put("sql", "findById");
        Optional<List<EmpEntity>> emps = findByParams(params);
        Optional<EmpEntity> emp = emps.map(x -> x.get(0));
        getCache().ifPresent(c -> emp.ifPresent(o -> c.put(cache_key, o)));
        return emp;
    }

    public Optional<List<EmpEntity>> findEmpsByOrg(OrgEntity org) {
        final String cache_key = String.format("ORG_EMPS_%s", org.getId());
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().get(cache_key, Object.class);
            if (cache_val != null) return Optional.of((List<EmpEntity>) cache_val);
        }
        Map<String, Object> params = org.toParamMap();
        params.put("sql", "findAllByOrg");
        Optional<List<EmpEntity>> emps = findByParams(params);
        if (emps.isPresent() && getCache().isPresent()) {
            getCache().get().put(cache_key, emps.get());
        }
        return emps;
    }

    public Optional<List<EmpEntity>> findEmpsByStore(StoEntity store) {
        final String cache_key = String.format("STORE_EMPS_%s", store.getId());
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().get(cache_key, Object.class);
            if (cache_val != null) return Optional.of((List<EmpEntity>) cache_val);
        }
        Map<String, Object> params = store.toParamMap();
        params.put("sql", "findAllByStore");
        Optional<List<EmpEntity>> emps = findByParams(params);
        if (emps.isPresent() && getCache().isPresent()) {
            getCache().get().put(cache_key, emps.get());
        }
        return emps;
    }

    public Optional<List<EmpEntity>> findEmpsByStore(StoEntity store, Collection<Integer> empIds) {
        if (CollectionUtils.isEmpty(empIds)) return Optional.empty();
        final String cache_key = String.format("STORE_EMPS_%s", store.getId());
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().get(cache_key, Object.class);
            if (cache_val != null) return Optional.of((List<EmpEntity>) cache_val);
        }
        Map<String, Object> params = store.toParamMap();
        params.put("sql", "findAllByStore");
        Optional<List<EmpEntity>> emps = findByParams(params);
        if (emps.isPresent() && getCache().isPresent()) {
            getCache().get().put(cache_key, emps.get());
        }
        if (!emps.isPresent()) return Optional.empty();
        List<EmpEntity> sub_list = emps.get().stream().filter(x -> empIds.contains(x.getId())).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isNotEmpty(sub_list) ? sub_list : null);
    }

    private Optional<List<EmpEntity>> findByParams(Map<String, Object> params) {
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    @Override
    protected RowMapper<EmpEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<EmpEntity> {
        @Override
        public EmpEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new EmpEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
