package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaleRecord4EmployeeEntity extends BaseEntity<Integer> {

    private final List<SaleSubRecord> saleRecords;
    private final String subRecordIds, oldSaleRecordId;
    private final Integer saleStoreId, companyId, memberStoreId, memberId, srvEmpId, goodsNum;
    private final List<Integer> saleEmpIds;
    private final double totalCardPrice, totalSalePrice;
    private final LocalDateTime saleDateTime;

    Integer getCompanyId() {
        return companyId;
    }

    Integer getMemberStoreId() {
        return memberStoreId;
    }

    /**
     * FK OOXX
     *
     * @param id        OO
     * @param resultSet XX
     * @throws RuntimeException OXOX
     */
    SaleRecord4EmployeeEntity(Integer id, ResultSet resultSet) throws RuntimeException {
        super(id);
        try {
            this.companyId = resultSet.getInt("company_id");
            this.saleStoreId = resultSet.getInt("saleStoreId");
            this.memberStoreId = resultSet.getInt("memberStoreId");
            this.subRecordIds = resultSet.getString("subRecordIds");
            this.oldSaleRecordId = resultSet.getString("old_sale_record_id");
            this.memberId = ResultSetUtil.getOptObject(resultSet, "member_id", Long.class).orElse(0L).intValue();
            this.srvEmpId = ResultSetUtil.getOptObject(resultSet, "service_emp_id", Long.class).orElse(0L).intValue();
            List<Integer> _saleEmpIds = Lists.newArrayList();
            String[] args = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10"};
            for (String $it : args) {
                String empId = ResultSetUtil.getOptString(resultSet, String.format("sales_emp%s_id", $it), "0");
                if (!StringUtils.equals("0", empId)) _saleEmpIds.add(Integer.parseInt(empId));
            }
            this.saleEmpIds = CollectionUtils.isEmpty(_saleEmpIds) ? null : ImmutableList.copyOf(_saleEmpIds);
            if (!Strings.isNullOrEmpty(this.subRecordIds)) {
                String records_str = resultSet.getString("records");
                String[] values = StringUtils.split(records_str, '$');
                List<SaleSubRecord> _records = Stream.of(values).map(SaleSubRecord::new).collect(Collectors.toList());
                this.saleRecords = ImmutableList.copyOf(_records);
            } else {
                this.saleRecords = null;
            }
            this.goodsNum = CollectionUtils.isEmpty(this.saleRecords) ? 0 : this.saleRecords.stream().mapToInt(x -> x.goodsNum).sum();
            this.totalCardPrice = CollectionUtils.isEmpty(this.saleRecords) ? 0.0D : this.saleRecords.stream().mapToDouble(x -> x.cardPrice).sum();
            this.totalSalePrice = CollectionUtils.isEmpty(this.saleRecords) ? 0.0D : this.saleRecords.stream().mapToDouble(x -> x.salePrice).sum();
            this.saleDateTime = ResultSetUtil.getLocalDateTime(resultSet, "createTime");
        } catch (SQLException e) {
            throw new RuntimeException("还原对象 EmployeeAllotEntity 发生异常", e);
        }
    }

    String getOldSaleRecordId() {
        return oldSaleRecordId;
    }

    boolean hasSubRecord() {
        return !Strings.isNullOrEmpty(subRecordIds);
    }

    LocalDateTime getSaleDateTime() {
        return saleDateTime;
    }

    Optional<Integer> getMemberId() {
        return Optional.ofNullable(memberId == 0 ? null : memberId);
    }

    Integer getSaleStoreId() {
        return saleStoreId;
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

    int getGoodsNum() {
        return goodsNum;
    }

    double getTotalCardPrice() {
        return totalCardPrice;
    }

    double getTotalSalePrice() {
        return totalSalePrice;
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
                .add("totalCardPrice", totalCardPrice)
                .add("totalSalePrice", totalSalePrice)
                .add("saleRecords", saleRecords)
                .toString();
    }

    static class SaleSubRecord {
        private final int id, goodsNum;
        private final double cardPrice, salePrice;

        SaleSubRecord(String item) {
            String[] args = StringUtils.split(item, "^^");
            this.id = Integer.parseInt(args[0]);
            this.cardPrice = Double.parseDouble(args[1]);
            this.salePrice = Double.parseDouble(args[2]);
            this.goodsNum = Integer.parseInt(args[3]);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("cp", cardPrice)
                    .add("sp", salePrice)
                    .toString();
        }
    }
}
