package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaleRecord4EmployeeEntity extends BaseEntity<Integer> {

    private final List<SaleSubRecord> saleRecords;
    private final Integer saleStoreId, companyId, memberStoreId, memberId;
    private final Integer srvEmpId;
    private final List<Integer> saleEmpIds;
    private final double totalCardPrice, totalSalePrice;

    SaleRecord4EmployeeEntity(Integer id, ResultSet resultSet) throws RuntimeException {
        super(id);
        try {
            this.companyId = resultSet.getInt("company_id");
            this.saleStoreId = resultSet.getInt("saleStoreId");
            this.memberStoreId = resultSet.getInt("memberStoreId");
            this.memberId = ResultSetUtil.getOptObject(resultSet, "member_id", Long.class).orElse(0L).intValue();
            this.srvEmpId = ResultSetUtil.getOptObject(resultSet, "service_emp_id", Long.class).orElse(0L).intValue();
            List<Integer> _saleEmpIds = Lists.newArrayList();
            for (int i = 1; i < 4; i++) {
                int empId = ResultSetUtil.getOptObject(resultSet, String.format("old_emp0%d_id", i), Long.class).orElse(0L).intValue();
                if (empId != 0) _saleEmpIds.add(empId);
            }
            this.saleEmpIds = CollectionUtils.isEmpty(_saleEmpIds) ? null : ImmutableList.copyOf(_saleEmpIds);
            String records_str = resultSet.getString("records");
            String[] values = StringUtils.split(records_str, '$');
            List<SaleSubRecord> _records = Stream.of(values).map(SaleSubRecord::new).collect(Collectors.toList());
            this.saleRecords = ImmutableList.copyOf(_records);
            this.totalCardPrice = this.saleRecords.stream().mapToDouble(x -> x.cardPrice).sum();
            this.totalSalePrice = this.saleRecords.stream().mapToDouble(x -> x.salePrice).sum();
        } catch (SQLException e) {
            throw new RuntimeException("还原对象 EmployeeAllotEntity 发生异常", e);
        }
    }

    boolean isNoCross() {
        return saleStoreId.equals(memberStoreId);
    }

    boolean isCross() {
        return !saleStoreId.equals(memberStoreId);
    }

    boolean hasSvrEmp() {
        return this.srvEmpId != 0;
    }

    Integer getSrvEmpId() {
        return srvEmpId;
    }

    int getEmpCount() {
        return (hasSvrEmp() ? 1 : 0) + (CollectionUtils.isNotEmpty(this.saleEmpIds) ? this.saleEmpIds.size() : 0);
    }

    List<Integer> getSaleEmpIds() {
        return saleEmpIds;
    }

    boolean hasSaleEmps() {
        return CollectionUtils.isNotEmpty(this.saleEmpIds);
    }

    double getTotalCardPrice() {
        return totalCardPrice;
    }

    double getTotalSalePrice() {
        return totalCardPrice;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("saleStoreId", saleStoreId)
                .add("memberStoreId", memberStoreId)
                .add("memberId", memberId)
                .add("srvEmpId", srvEmpId)
                .add("saleEmpIds", saleEmpIds)
                .add("saleRecords", saleRecords)
                .toString();
    }

    static class SaleSubRecord {
        private final int id;
        private final double cardPrice, salePrice;

        SaleSubRecord(String item) {
            String[] args = StringUtils.split(item, "^^");
            this.id = Integer.parseInt(args[0]);
            this.cardPrice = Double.parseDouble(args[1]);
            this.salePrice = Double.parseDouble(args[2]);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("cardPrice", cardPrice)
                    .add("salePrice", salePrice)
                    .toString();
        }
    }
}
