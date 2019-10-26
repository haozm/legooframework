package com.legooframework.model.insurance.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import com.legooframework.model.insurance.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class InsurancePolicyService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(InsurancePolicyService.class);

    @Override
    protected Bundle getLocalBundle() {
        return getBean("insuranceBundle", Bundle.class);
    }


    public Integer insured(MemberEntity defrayer, MemberEntity accepter, String insuranceNo, Date insuredDate,
                           String paymentType, Double payAmount, String bankType, String backCardNo, String relationship,
                           List<String> insuranceInfos, String beneficiary, String remarks) {
        Optional<KvDictEntity> paymentType_opt = getBean(KvDictEntityAction.class).findByValue("PAYMENTTYPE", paymentType);
        Preconditions.checkState(paymentType_opt.isPresent(), "type=%s 对应的缴费模式不存在", paymentType);

        Optional<KvDictEntity> relationship_opt = getBean(KvDictEntityAction.class).findByValue("RELATIONSHIP", relationship);
        Preconditions.checkState(relationship_opt.isPresent(), "type=%s 对应的关系定义不存在", paymentType);

        BankCardEntity bankCard = getBean(BankCardEntityAction.class).insert(defrayer, bankType, backCardNo);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(insuranceInfos));
        List<InsuranceInfoEntity> insurances = Lists.newArrayList();
        insuranceInfos.forEach(x -> {
            String[] items = StringUtils.split(x, ',');
            Optional<KvDictEntity> insurance_type = getBean(KvDictEntityAction.class).findByValue("INSURANCE", items[0]);
            Preconditions.checkState(insurance_type.isPresent(), "type=%s 对应的保险不存在", items[0]);
            insurances.add(new InsuranceInfoEntity(insurance_type.get(), items[1], items[2]));
        });

        InsurancePolicyEntity policyEntity = new InsurancePolicyEntity(defrayer, accepter, insuranceNo, insuredDate,
                relationship_opt.get(), paymentType_opt.get(), payAmount, bankCard, insurances, beneficiary, remarks);
        getBean(InsurancePolicyEntityAction.class).insert(policyEntity);
        getBean(InsuranceInfoEntityAction.class).batchInsert(policyEntity, insurances);
        if (logger.isDebugEnabled())
            logger.debug(String.format("insured(%s) is ok", policyEntity));
        return policyEntity.getId();
    }

    public Integer insuredSelf(MemberEntity defrayer, String insuranceNo, Date insuredDate,
                               String paymentType, Double payAmount, String bankType, String backCardNo,
                               List<String> insuranceInfos, String beneficiary, String remarks) {
        BankCardEntity bankCard = getBean(BankCardEntityAction.class).insert(defrayer, bankType, backCardNo);
        Optional<KvDictEntity> paymentType_opt = getBean(KvDictEntityAction.class).findByValue("PAYMENTTYPE", paymentType);
        Preconditions.checkState(paymentType_opt.isPresent(), "type=%s 对应的缴费模式不存在", paymentType);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(insuranceInfos));
        List<InsuranceInfoEntity> insurances = Lists.newArrayList();
        insuranceInfos.forEach(x -> {
            String[] items = StringUtils.split(x, ',');
            Optional<KvDictEntity> insurance_type = getBean(KvDictEntityAction.class).findByValue("INSURANCE", items[0]);
            Preconditions.checkState(insurance_type.isPresent(), "type=%s 对应的保险不存在", items[0]);
            insurances.add(new InsuranceInfoEntity(insurance_type.get(), items[1], items[2]));
        });
        InsurancePolicyEntity policyEntity = new InsurancePolicyEntity(defrayer, insuranceNo, insuredDate,
                paymentType_opt.get(), payAmount, bankCard, insurances, beneficiary, remarks);
        getBean(InsurancePolicyEntityAction.class).insert(policyEntity);
        getBean(InsuranceInfoEntityAction.class).batchInsert(policyEntity, insurances);
        if (logger.isDebugEnabled())
            logger.debug(String.format("insured(%s) is ok", policyEntity));
        return policyEntity.getId();
    }

    public InsurancePolicyDto findByInsuranceNo(String insuranceNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(insuranceNo), "入参 %s 不可以为空...", insuranceNo);
        Optional<InsurancePolicyEntity> policy = getBean(InsurancePolicyEntityAction.class).findByInsuranceNo(insuranceNo);
        Preconditions.checkState(policy.isPresent(), "保单号%s对应的保单信息缺失...", insuranceNo);
        List<InsuranceInfoEntity> list = getBean(InsuranceInfoEntityAction.class).loadByInsurance(policy.get());
        return new InsurancePolicyDto(policy.get(), list);
    }

}
