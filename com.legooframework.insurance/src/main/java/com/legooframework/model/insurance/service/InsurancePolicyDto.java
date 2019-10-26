package com.legooframework.model.insurance.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.insurance.entity.InsuranceInfoEntity;
import com.legooframework.model.insurance.entity.InsurancePolicyEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

public class InsurancePolicyDto {

    private final InsurancePolicyEntity policy;
    private final List<InsuranceInfoEntity> insurList;

    InsurancePolicyDto(InsurancePolicyEntity policy, List<InsuranceInfoEntity> insurList) {
        this.policy = policy;
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(insurList));
        this.insurList = insurList;
    }

    public InsurancePolicyEntity getPolicy() {
        return policy;
    }

    public InsuranceInfoEntity getMainIns() {
        Optional<InsuranceInfoEntity> ins = insurList.stream().filter(InsuranceInfoEntity::isPrimary).findFirst();
        Preconditions.checkState(ins.isPresent(), "缺失保单主险种...");
        return ins.get();
    }

    public List<InsuranceInfoEntity> getInsurList() {
        return insurList;
    }
}
