package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CrmEmployeeEntityAction extends BaseEntityAction<CrmEmployeeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmEmployeeEntityAction.class);

    public CrmEmployeeEntityAction() {
        super("CrmAdapterCache");
    }

    @SuppressWarnings("unchecked")
    public Optional<List<CrmEmployeeEntity>> loadAllByStore(CrmStoreEntity store) {
        Preconditions.checkNotNull(store, "所属门店不可以为空值...");
        final String cache_key = String.format("%s_bystore_%s_%s", getModelName(), store.getCompanyId(), store.getId());
        if (getCache().isPresent()) {
            List<CrmEmployeeEntity> employees = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(employees)) {
                if (logger.isDebugEnabled())
                    logger.debug("loadAllByStore(store::%s) from cache by %s", store.getName(), cache_key);
                return Optional.of(employees);
            }
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId());
        Optional<List<CrmEmployeeEntity>> employees = queryForEntities("loadAllByStore", params, getRowMapper());
        if (getCache().isPresent() && employees.isPresent())
            getCache().get().put(cache_key, employees.get());
        return employees;
    }

    public Optional<List<CrmEmployeeEntity>> loadEnableByStore(CrmStoreEntity store) {
        Optional<List<CrmEmployeeEntity>> employees = loadAllByStore(store);
        return employees.map(crmEmployeeEntities -> crmEmployeeEntities.stream().
                filter(CrmEmployeeEntity::isEnabled).collect(Collectors.toList()));
    }

    @Override
    protected RowMapper<CrmEmployeeEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CrmEmployeeEntity> {
        @Override
        public CrmEmployeeEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new CrmEmployeeEntity(resultSet.getInt("id"), resultSet);
        }
    }

}
