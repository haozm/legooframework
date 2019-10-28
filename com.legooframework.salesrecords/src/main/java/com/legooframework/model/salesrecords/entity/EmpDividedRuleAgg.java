package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

public class EmpDividedRuleAgg {

    private final StoEntity store;
    private final List<List<EmpDividedRuleEntity.Divided>> memberRule;
    private final List<List<EmpDividedRuleEntity.Divided>> noMemberRule;
    private final List<List<EmpDividedRuleEntity.Divided>> crossMemberRule;
    private final List<List<EmpDividedRuleEntity.Divided>> crossNoMemberRule;

    EmpDividedRuleAgg(StoEntity store, EmpDividedRuleEntity storeRule, EmpDividedRuleEntity companyRule) {
        this.store = store;
        if (storeRule != null) {
            this.memberRule = storeRule.getMemberRule();
            this.noMemberRule = storeRule.getNoMemberRule();
        } else {
            this.memberRule = companyRule.getMemberRule();
            this.noMemberRule = companyRule.getNoMemberRule();
        }
        this.crossMemberRule = companyRule.getCrossMemberRule();
        this.crossNoMemberRule = companyRule.getCrossNoMemberRule();
    }

    // 业务分配
    void allocation(EmployeeAllotEntity employeeAllot) {
        if (employeeAllot.isNoCross() && employeeAllot.hasSvrEmp()) {
            if (CollectionUtils.isEmpty(this.memberRule))
                throw new AllocationEmpException(String.format("门店%d未配置分配规则....", store.getId()));
            int emp_count = employeeAllot.getEmpCount();
            Optional<List<EmpDividedRuleEntity.Divided>> divideds = this.memberRule.stream()
                    .filter(x -> x.size() == emp_count).findFirst();
            if (!divideds.isPresent())
                throw new AllocationEmpException(String.format("门店%d缺少数量为%d的会员分配规则", store.getId(), emp_count));
            double total_care_price = employeeAllot.getTotalCardPrice();
            double total_sale_price = employeeAllot.getTotalSalePrice();
            Optional<EmpDividedRuleEntity.Divided> svr_divided = divideds.get().stream()
                    .filter(EmpDividedRuleEntity.Divided::isSvrEmp).findFirst();
            Preconditions.checkState(svr_divided.isPresent(), "数据异常....");
            List<AllocationRes> care_allocations = Lists.newArrayList();
            care_allocations.add(new AllocationRes(svr_divided.get(), employeeAllot.getSrvEmpId(), total_care_price));
            List<AllocationRes> sale_allocations = Lists.newArrayList();
            sale_allocations.add(new AllocationRes(svr_divided.get(), employeeAllot.getSrvEmpId(), total_sale_price));
            if (employeeAllot.hasSaleEmps()) {
                int index = 1;
                for (Integer empId : employeeAllot.getSaleEmpIds()) {
                    care_allocations.add(new AllocationRes(divideds.get().get(index), empId, total_care_price));
                    sale_allocations.add(new AllocationRes(divideds.get().get(index), empId, total_sale_price));
                    index++;
                }
            }
        } else if (employeeAllot.isNoCross() && !employeeAllot.hasSvrEmp()) {
            if (CollectionUtils.isEmpty(this.noMemberRule))
                throw new AllocationEmpException(String.format("门店%d未配置分配规则....", store.getId()));
            int emp_count = employeeAllot.getEmpCount();
            Optional<List<EmpDividedRuleEntity.Divided>> divideds = this.noMemberRule.stream()
                    .filter(x -> x.size() == emp_count).findFirst();
            if (!divideds.isPresent())
                throw new AllocationEmpException(String.format("门店%d缺少数量为%d的散客分配规则", store.getId(), emp_count));
            double total_care_price = employeeAllot.getTotalCardPrice();
            double total_sale_price = employeeAllot.getTotalSalePrice();
            List<AllocationRes> care_allocations = Lists.newArrayList();
            List<AllocationRes> sale_allocations = Lists.newArrayList();
            int index = 0;
            for (Integer empId : employeeAllot.getSaleEmpIds()) {
                care_allocations.add(new AllocationRes(divideds.get().get(index), empId, total_care_price));
                sale_allocations.add(new AllocationRes(divideds.get().get(index), empId, total_sale_price));
                index++;
            }
        } else if (employeeAllot.isCross() && employeeAllot.hasSvrEmp()) {
            if (CollectionUtils.isEmpty(this.crossMemberRule))
                throw new AllocationEmpException(String.format("门店%d未配置分配规则....", store.getId()));
            int emp_count = employeeAllot.getEmpCount();
            Optional<List<EmpDividedRuleEntity.Divided>> divideds = this.crossMemberRule.stream()
                    .filter(x -> x.size() == emp_count).findFirst();
            if (!divideds.isPresent())
                throw new AllocationEmpException(String.format("门店%d缺少数量为%d的跨店分配规则", store.getId(), emp_count));
            double total_care_price = employeeAllot.getTotalCardPrice();
            double total_sale_price = employeeAllot.getTotalSalePrice();

        } else if (employeeAllot.isCross() && !employeeAllot.hasSvrEmp()) {

        } else {
            throw new AllocationEmpException(String.format("异常数据信息 %s", employeeAllot));
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", store.getCompanyId())
                .add("storeId", store.getId())
                .add("memberRule", memberRule)
                .add("noMemberRule", noMemberRule)
                .add("crossMemberRule", crossMemberRule)
                .add("crossNoMemberRule", crossNoMemberRule)
                .toString();
    }
}
