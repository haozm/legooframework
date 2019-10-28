package com.legooframework.model.salesrecords.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class EmployeeAllotEntityAction extends BaseEntityAction<EmployeeAllotEntity> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeAllotEntityAction.class);

    public EmployeeAllotEntityAction() {
        super(null);
    }

    @Override
    public Optional<EmployeeAllotEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("sql", "findById");
        Optional<EmployeeAllotEntity> optional = super.queryForEntity("quer4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%d) return %s", id, optional.orElse(null)));
        return optional;
    }

    @Override
    protected RowMapper<EmployeeAllotEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<EmployeeAllotEntity> {
        @Override
        public EmployeeAllotEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new EmployeeAllotEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
