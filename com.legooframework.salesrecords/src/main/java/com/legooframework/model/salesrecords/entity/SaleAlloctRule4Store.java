package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class SaleAlloctRule4Store {

    private static final Logger logger = LoggerFactory.getLogger(SaleAlloctRule4Store.class);

    private final StoEntity store;
    private final List<List<SaleAlloctRuleEntity.Rule>> memberRule;
    private final List<List<SaleAlloctRuleEntity.Rule>> noMemberRule;
    private final List<List<SaleAlloctRuleEntity.Rule>> crossMemberRule;
    private final List<List<SaleAlloctRuleEntity.Rule>> crossNoMemberRule;

    SaleAlloctRule4Store(StoEntity store, SaleAlloctRuleEntity storeRule, SaleAlloctRuleEntity companyRule) {
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
    public void allocation(SaleAlloct4EmpResult saleAlloct4EmpResult) {
        SaleRecord4EmployeeEntity employeeAllot = saleAlloct4EmpResult.getEmployeeAllot();
        int emp_count = employeeAllot.getEmpCount();
        if (emp_count == 0) {
            saleAlloct4EmpResult.setException(new RuntimeException("该销售单无导购数据信息....."));
            return;
        }
        try {
            if (employeeAllot.isNoCross() && employeeAllot.hasSvrEmp()) {
                if (CollectionUtils.isEmpty(this.memberRule))
                    throw new AllocatEmpException(String.format("门店%d未配置分配规则....", store.getId()));
                Optional<List<SaleAlloctRuleEntity.Rule>> rulesOpt = this.memberRule.stream()
                        .filter(x -> x.size() == emp_count).findFirst();
                if (!rulesOpt.isPresent())
                    throw new AllocatEmpException(String.format("门店%d缺少数量为%d的会员分配规则", store.getId(), emp_count));
                saleAlloct4EmpResult.memberDevide(rulesOpt.get());
            } else if (employeeAllot.isNoCross() && !employeeAllot.hasSvrEmp()) {
                if (CollectionUtils.isEmpty(this.noMemberRule))
                    throw new AllocatEmpException(String.format("门店%d未配置分配规则....", store.getId()));
                Optional<List<SaleAlloctRuleEntity.Rule>> rulesOpt = this.noMemberRule.stream()
                        .filter(x -> x.size() == emp_count).findFirst();
                if (!rulesOpt.isPresent())
                    throw new AllocatEmpException(String.format("门店%d缺少数量为%d的散客分配规则", store.getId(), emp_count));
                saleAlloct4EmpResult.noMmberDevide(rulesOpt.get());
            } else if (employeeAllot.isCross() && employeeAllot.hasSvrEmp()) {
                if (CollectionUtils.isEmpty(this.crossMemberRule))
                    throw new AllocatEmpException(String.format("门店%d未配置分配规则....", store.getId()));
                Optional<List<SaleAlloctRuleEntity.Rule>> rulesOpt = this.crossMemberRule.stream()
                        .filter(x -> x.size() == emp_count).findFirst();
                if (!rulesOpt.isPresent())
                    throw new AllocatEmpException(String.format("门店%d缺少数量为%d的跨店分配规则", store.getId(), emp_count));
                saleAlloct4EmpResult.crsMmberDevide(rulesOpt.get());
            } else if (employeeAllot.isCross() && !employeeAllot.hasSvrEmp()) {
                if (CollectionUtils.isEmpty(this.crossNoMemberRule))
                    throw new AllocatEmpException(String.format("门店%d未配置分配规则....", store.getId()));
                Optional<List<SaleAlloctRuleEntity.Rule>> rulesOpt = this.crossNoMemberRule.stream()
                        .filter(x -> x.size() == emp_count).findFirst();
                if (!rulesOpt.isPresent())
                    throw new AllocatEmpException(String.format("门店%d缺少数量为%d的跨店非会员分配规则", store.getId(), emp_count));
                saleAlloct4EmpResult.crsNoMmberDevide(rulesOpt.get());
            } else {
                throw new RuntimeException("未知异常...");
            }
        } catch (Exception e) {
            logger.error(String.format("分配销售单%s发生异常", employeeAllot), e);
            saleAlloct4EmpResult.setException(e);
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
