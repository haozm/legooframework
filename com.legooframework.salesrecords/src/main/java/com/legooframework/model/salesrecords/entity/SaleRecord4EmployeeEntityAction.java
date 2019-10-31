package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
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

    public Optional<List<SaleRecord4EmployeeEntity>> findByStore(StoEntity store) {
        Map<String, Object> params = store.toParamMap();
        params.put("sql", "findByStore");
        Optional<List<SaleRecord4EmployeeEntity>> optional = super.queryForEntities("quer4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByStore(%d) return %s", store.getId(), optional.map(List::size).orElse(0)));
        return optional;
    }

    public Optional<List<SaleRecord4EmployeeEntity>> findByStoreWithPeriod(StoEntity store, LocalDate start, LocalDate end) {
        Map<String, Object> params = store.toParamMap();
        params.put("sql", "findByStoreWithPeriod");
        params.put("startDate", start.toString("yyyy-MM-dd 00:00:00"));
        params.put("endDate", end.toString("yyyy-MM-dd 23:59:59"));
        Optional<List<SaleRecord4EmployeeEntity>> optional = super.queryForEntities("quer4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByStoreWithPeriod(%d) return %s", store.getId(), optional.map(List::size).orElse(0)));
        return optional;
    }

    public long loadUndoCountByCompany(OrgEntity company, LocalDate startDate) {
        String query_sql = "SELECT COUNT(sea.sale_record_id) FROM acp.sales_employee_allot AS sea \n " +
                "LEFT JOIN acp.crm_salesubrecord sub ON sub.saleRecord_id = sea.sale_record_id \n" +
                "WHERE sea.allot_status = 0 AND sea.company_id = %d AND sea.createTime > '%s 00:00:00'";
        query_sql = String.format(query_sql, company.getId(), startDate.toString("yyyy-MM-dd"));
        return super.queryForLong(query_sql, 0L);
    }

    public Optional<List<SaleRecord4EmployeeEntity>> findUndoByCompany(OrgEntity company) {
        Map<String, Object> params = company.toParamMap();
        params.put("sql", "findUndoByCompany");
        Optional<List<SaleRecord4EmployeeEntity>> optional = super.queryForEntities("quer4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findUndoByCompany(%d) return %s", company.getId(), optional.map(List::size).orElse(0)));
        return optional;
    }

    void updateStatus(Collection<Integer> saleRecordIds) {
        if (CollectionUtils.isEmpty(saleRecordIds)) return;
        String update_sql = "UPDATE acp.sales_employee_allot SET allot_status = 1 WHERE sale_record_id IN (%s)";
        update_sql = String.format(update_sql, Joiner.on(',').join(saleRecordIds));
        super.update(update_sql, null);
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateStatus(%d) %s", saleRecordIds.size(), update_sql));
    }

    @Override
    protected RowMapper<SaleRecord4EmployeeEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    static class RowMapperImpl implements RowMapper<SaleRecord4EmployeeEntity> {
        @Override
        public SaleRecord4EmployeeEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SaleRecord4EmployeeEntity(res.getInt("id"), res);
        }
    }
}
