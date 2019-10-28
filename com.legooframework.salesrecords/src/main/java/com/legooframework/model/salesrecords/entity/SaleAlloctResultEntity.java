package com.legooframework.model.salesrecords.entity;

import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class SaleAlloctResultEntity extends BaseEntity<Long> {

    private Integer companyId, storeId, employeeId, memberId, saleId, orderType, employeeNum;
    private boolean error, detail;
    private String allotRule, allotMsg;
    private double cardAmount, saleAmount;

    SaleAlloctResultEntity(StoEntity store, SaleRecord4EmployeeEntity saleRecord4Employee, int orderType,
                           List<SaleAlloctRuleEntity.Rule> rules, boolean error) {
        super(0L);
        this.companyId = store.getCompanyId();
        this.saleId = store.getId();
        this.saleId = saleRecord4Employee.getId();
        this.orderType = orderType;
        this.memberId = saleRecord4Employee.getMemberId().orElse(0);
        this.employeeNum = saleRecord4Employee.getEmpCount();
        this.detail = false;
        this.allotRule = CollectionUtils.isEmpty(rules) ? null : rules.toString();
        this.cardAmount = 0.00D;
        this.saleAmount = 0.00D;
        this.employeeId = 0;
        this.error = error;
    }

    SaleAlloctResultEntity(SaleAlloctResultEntity totalRec, Integer employeeId, SaleAlloctRuleEntity.Rule rule, double cardAmount,
                           double saleAmount) {
        super(0L);
        this.companyId = totalRec.companyId;
        this.saleId = totalRec.storeId;
        this.saleId = totalRec.saleId;
        this.orderType = totalRec.orderType;
        this.memberId = totalRec.memberId;
        this.employeeNum = totalRec.employeeNum;
        this.detail = true;
        this.allotRule = rule.toString();
        this.cardAmount = cardAmount;
        this.saleAmount = saleAmount;
        this.employeeId = employeeId;
        this.error = false;
        this.allotMsg = "OK";
    }
}
