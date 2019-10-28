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

public class SaleRecord4EmployeeEntityAction extends BaseEntityAction<SaleRecord4EmployeeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecord4EmployeeEntityAction.class);

    public SaleRecord4EmployeeEntityAction() {
        super(null);
    }

    @Override
    public Optional<SaleRecord4EmployeeEntity> findById(Object id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("sql", "findById");
        Optional<SaleRecord4EmployeeEntity> optional = super.queryForEntity("quer4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%d) return %s", id, optional.orElse(null)));
        return optional;
    }

    @Override
    protected RowMapper<SaleRecord4EmployeeEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SaleRecord4EmployeeEntity> {
        @Override
        public SaleRecord4EmployeeEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new SaleRecord4EmployeeEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
