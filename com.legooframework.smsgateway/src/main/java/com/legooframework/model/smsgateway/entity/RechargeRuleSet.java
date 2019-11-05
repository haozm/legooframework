package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.legooframework.model.covariant.entity.OrgEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 充值规则合集
 */
public class RechargeRuleSet {

    private List<RechargeRuleEntity> rules;

    private static Comparator<RechargeRuleEntity> comparator = Comparator.comparingDouble(RechargeRuleEntity::getUnitPrice);

    RechargeRuleSet(List<RechargeRuleEntity> rules) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(rules), "计费规则不可以为空值...");
        this.rules = ImmutableList.copyOf(rules);
    }

    /**
     * 选择优惠力度最大的作为扣费标准
     *
     * @param company
     * @param rechargeAmount
     * @return
     */
    public Optional<RechargeRuleEntity> getSuitableRule(OrgEntity company, long rechargeAmount) {
        List<RechargeRuleEntity> list = this.rules.stream()
                .filter(x -> x.isSuitable(company, rechargeAmount)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) return Optional.empty();
        list.sort(comparator);
        return Optional.of(list.get(0));
    }

}
