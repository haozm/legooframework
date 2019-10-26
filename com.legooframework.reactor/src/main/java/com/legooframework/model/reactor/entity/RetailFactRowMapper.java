package com.legooframework.model.reactor.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class RetailFactRowMapper implements RowMapper<RetailFactEntity> {

    @Override
    public RetailFactEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        DateTime datetime = ResultSetUtil.getDateTime(rs, "createtime");
        String sale_goods = ResultSetUtil.getString(rs, "saleInfo");
        List<RetailFactEntity.SaleGoods> saleGoods = Lists.newArrayList();
        Stream.of(StringUtils.split(sale_goods, "$$")).forEach(sales -> {
            RetailFactEntity.SaleGoods sale = new RetailFactEntity.SaleGoods(StringUtils.split(sales, "^^"));
            saleGoods.add(sale);
        });
        return new RetailFactEntity(rs.getLong("id"), datetime, saleGoods, rs);
    }

}
