package com.legooframework.model.salesrecords.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeAllotEntityAction extends BaseEntityAction<EmployeeAllotEntity> {

    public EmployeeAllotEntityAction() {
        super(null);
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
