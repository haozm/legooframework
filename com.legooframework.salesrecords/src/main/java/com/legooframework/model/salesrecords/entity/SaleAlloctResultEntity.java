package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SaleAlloctResultEntity extends BaseEntity<Long> implements BatchSetter {

    private Integer companyId, storeId, saleRecordId, employeeId, orderType, empNum, empType, memberId, memberStoreId, goodsSum;
    private boolean error, detail;
    private String allotRule, allotMsg, oldSaleRecordId;
    private double cardAmount, saleAmount;
    private LocalDateTime saleDateTime;

    Integer getCompanyId() {
        return companyId;
    }

    Integer getSaleRecordId() {
        return saleRecordId;
    }

    SaleAlloctResultEntity(SaleRecord4EmployeeEntity saleRecord4Employee, int orderType,
                           List<SaleAlloctRuleEntity.Rule> rules, boolean error, String errMsg) {
        super(0L);
        this.companyId = saleRecord4Employee.getCompanyId();
        this.storeId = saleRecord4Employee.getSaleStoreId();
        this.saleRecordId = saleRecord4Employee.getId();
        this.orderType = orderType;
        this.memberId = saleRecord4Employee.getMemberId().orElse(0);
        this.empNum = saleRecord4Employee.getEmpCount();
        this.detail = false;
        this.memberStoreId = saleRecord4Employee.getMemberStoreId();
        this.empType = 0;
        this.oldSaleRecordId = saleRecord4Employee.getOldSaleRecordId();
        this.allotRule = CollectionUtils.isEmpty(rules) ? null : rules.toString();
        this.cardAmount = saleRecord4Employee.getTotalCardPrice();
        this.saleAmount = saleRecord4Employee.getTotalSalePrice();
        this.saleDateTime = saleRecord4Employee.getSaleDateTime();
        this.goodsSum = saleRecord4Employee.getGoodsNum();
        this.employeeId = 0;
        this.error = error;
        this.allotMsg = errMsg;
    }

    SaleAlloctResultEntity(SaleAlloctResultEntity totalRec, Integer employeeId, SaleAlloctRuleEntity.Rule rule, double cardAmount,
                           double saleAmount) {
        super(0L);
        this.companyId = totalRec.companyId;
        this.storeId = totalRec.storeId;
        this.saleRecordId = totalRec.saleRecordId;
        this.orderType = totalRec.orderType;
        this.memberId = totalRec.memberId;
        this.goodsSum = totalRec.goodsSum;
        this.memberStoreId = totalRec.memberStoreId;
        this.oldSaleRecordId = totalRec.oldSaleRecordId;
        this.saleDateTime = totalRec.saleDateTime;
        this.empType = rule.getType();
        this.empNum = 1;
        this.detail = true;
        this.allotRule = rule.toString();
        this.cardAmount = cardAmount;
        this.saleAmount = saleAmount;
        this.employeeId = employeeId;
        this.error = false;
        this.allotMsg = "OK";
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        company_id, store_id, sale_record_id, employee_id, employee_type, employee_count, order_type, member_id,
        ps.setObject(1, companyId);
        ps.setObject(2, storeId);
        ps.setObject(3, saleRecordId);
        ps.setObject(4, employeeId);
        ps.setObject(5, empType);
        ps.setObject(6, empNum);
        ps.setObject(7, orderType);
        ps.setObject(8, memberId == null ? 0 : memberId);
//        error_tag, is_detail, allot_rule, card_amount, sale_amount, allot_msg, sale_date, delete_flag, tenant_id
        ps.setObject(9, error ? 1 : 0);
        ps.setObject(10, detail ? 1 : 0);
        ps.setObject(11, allotRule == null ? null : allotRule.toString());
        ps.setObject(12, cardAmount);
        ps.setObject(13, saleAmount);
        ps.setObject(14, allotMsg);
        ps.setObject(15, saleDateTime.toDate());
        ps.setObject(16, companyId);
        ps.setObject(17, goodsSum);
        ps.setObject(18, oldSaleRecordId);
        ps.setObject(19, memberStoreId == null ? 0 : memberStoreId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("saleRecordId", saleRecordId)
                .add("employeeId", employeeId)
                .add("orderType", orderType)
                .add("empNum", empNum)
                .add("empType", empType)
                .add("memberId", memberId)
                .add("memberStoreId", memberStoreId)
                .add("error", error)
                .add("detail", detail)
                .add("allotRule", allotRule)
                .add("allotMsg", allotMsg)
                .add("cardAmount", cardAmount)
                .add("saleAmount", saleAmount)
                .add("goodsSum", goodsSum)
                .add("saleDateTime", saleDateTime)
                .add("oldSaleRecordId", oldSaleRecordId)
                .add("memberStoreId", memberStoreId)
                .toString();
    }
}
