package com.legooframework.model.salesrecords.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleGoodsEntityAction extends BaseEntityAction<SaleGoodsEntity> {

    public SaleGoodsEntityAction() {
        super(null);
    }

    @Override
    protected RowMapper<SaleGoodsEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<SaleGoodsEntity> {
        @Override
        public SaleGoodsEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new SaleGoodsEntity(resultSet.getString("oldGoodsId"), resultSet.getInt("companyId"), resultSet);
        }
    }
}
