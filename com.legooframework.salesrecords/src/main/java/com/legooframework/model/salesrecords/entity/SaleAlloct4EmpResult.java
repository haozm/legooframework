package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SaleAlloct4EmpResult {

    private final SaleRecord4EmployeeEntity employeeAllot;
    private List<SaleAlloctRuleEntity.Rule> rules;
    private int type; // 1 2 3 4
    private final List<Result> results = Lists.newArrayList();
    private boolean error;
    private String message;

    void setException(Exception e) {
        this.error = true;
        this.message = e.getMessage();
    }

    SaleAlloct4EmpResult(SaleRecord4EmployeeEntity employeeAllot) {
        this.employeeAllot = employeeAllot;
        this.error = false;
    }

    void memberDevide(List<SaleAlloctRuleEntity.Rule> rules) {
        this.type = 1;
        this.rules = rules;
        allocationSvrEmp();
        allocationSaleEmps();
    }

    void noMmberDevide(List<SaleAlloctRuleEntity.Rule> rules) {
        this.type = 2;
        this.rules = rules;
        allocationSaleEmps();
    }

    void crsMmberDevide(List<SaleAlloctRuleEntity.Rule> rules) {
        this.type = 3;
        this.rules = rules;
        allocationSvrEmp();
        allocationSaleEmps();
    }

    void crsNoMmberDevide(List<SaleAlloctRuleEntity.Rule> rules) {
        this.type = 4;
        this.rules = rules;
        allocationSaleEmps();
    }

    // 服务导购
    private void allocationSvrEmp() {
        Integer employeeId = this.employeeAllot.getSrvEmpId();
        Optional<SaleAlloctRuleEntity.Rule> rule_opt = rules.stream().filter(SaleAlloctRuleEntity.Rule::isSvrEmp).findFirst();
        Preconditions.checkState(rule_opt.isPresent(), "数据异常....,不存在对应的导购分配规则");
        this.results.add(new Result(rule_opt.get(), employeeId, employeeAllot));
    }

    // 销售导购
    private void allocationSaleEmps() {
        if (CollectionUtils.isEmpty(employeeAllot.getSaleEmpIds())) return;
        List<SaleAlloctRuleEntity.Rule> sub_rules = rules.stream().filter(x -> !x.isSvrEmp()).collect(Collectors.toList());
        int index = 0;
        for (Integer empId : employeeAllot.getSaleEmpIds()) {
            this.results.add(new Result(sub_rules.get(index), empId, employeeAllot));
            index++;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("employeeAllot", employeeAllot.getId())
                .add("rules", rules)
                .add("type", type)
                .add("results", results.size())
                .add("error", error)
                .add("message", message)
                .toString();
    }

    static class Result {
        private final Integer empId;
        private final double careAmount;
        private final double saleAmount;
        private final SaleAlloctRuleEntity.Rule rule;


        Result(SaleAlloctRuleEntity.Rule rule, Integer empId, SaleRecord4EmployeeEntity allot) {
            this.rule = rule;
            this.empId = empId;
            this.careAmount = this.rule.allocation(allot.getTotalCardPrice());
            this.saleAmount = this.rule.allocation(allot.getTotalSalePrice());
        }


    }
}
