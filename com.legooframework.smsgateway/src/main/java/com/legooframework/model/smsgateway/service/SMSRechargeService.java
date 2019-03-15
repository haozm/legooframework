package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.smsgateway.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SMSRechargeService extends SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SMSRechargeService.class);

    /**
     * 公司充值
     *
     * @param companyId      公司ID
     * @param rechargeAmount 充值金额
     */
    public void rechargeByCompany(Integer companyId, long rechargeAmount, RechargeType rechargeType) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeByCompany(%s,%s)", companyId, rechargeAmount));
        CrmOrganizationEntity company = getCompany(companyId);
        RechargeRuleEntity rule = getRechargeRule(company, rechargeAmount);
        RechargeRes rechargeRes = rechargeByCompany(companyId, rechargeAmount, rule, rechargeType);
        addBalance(rechargeRes);
    }

    public void freecharge(Integer companyId, String storeGroupId, Integer storId, int totalQuantity) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("freecharge(%s,%s,%s,%s)", companyId, storeGroupId, storId, totalQuantity));
        CrmOrganizationEntity company = getCompany(companyId);
        CrmStoreEntity store = storId == null ? null : getStore(companyId, storId);
        RechargeRes rechargeRes = getBean(RechargeDetailEntityAction.class).freecharge(company, store, storeGroupId, totalQuantity);
        addBalance(rechargeRes);
    }

    private void addBalance(RechargeRes rechargeRes) {
        Optional<RechargeDetailEntity> recharge = getBean(RechargeDetailEntityAction.class).findById(rechargeRes.getRechargeId());
        Preconditions.checkState(recharge.isPresent(), "保存充值流水失败...,Id=%s", rechargeRes);
        getBean(RechargeBalanceEntityAction.class).addBalance(recharge.get(), recharge.get().getTotalQuantity());
    }

    /**
     * 公司一次性充值
     *
     * @param companyId      公司
     * @param rechargeAmount 金额
     * @param unitPrice      单价
     * @param remarke        备注
     */
    public void rechargeByCompanyOnce(Integer companyId, long rechargeAmount, double unitPrice, String remarke,
                                      RechargeType rechargeType) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeByCompanyOnce(%s,%s,%s,%s)", companyId, rechargeAmount,
                    unitPrice, remarke));
        Optional<CrmOrganizationEntity> company_opt = getBean(CrmOrganizationEntityAction.class).findCompanyById(companyId);
        Preconditions.checkState(company_opt.isPresent());
        CrmOrganizationEntity company = company_opt.get();
        RechargeRuleEntity rule = createTemporaryRule(company, rechargeAmount, unitPrice, remarke);
        RechargeRes rechargeRes = rechargeByCompany(companyId, rechargeAmount, rule, rechargeType);
        addBalance(rechargeRes);
    }

    /**
     * 组织充值
     *
     * @param companyId      公司
     * @param storeGroupId   机构
     * @param rechargeAmount 金额
     */
    public void rechargeByStoreGroup(Integer companyId, String storeGroupId, long rechargeAmount, RechargeType rechargeType) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeByStoreGroup(%s,%s,%s)", companyId, storeGroupId, rechargeAmount));
        CrmOrganizationEntity company = getCompany(companyId);
        RechargeRuleEntity rule = getRechargeRule(company, rechargeAmount);
        RechargeRes rechargeRes = null;
        switch (rechargeType) {
            case Recharge:
                rechargeRes = getRechargeAction().recharge(company, null, storeGroupId, rule, rechargeAmount);
                break;
            case Precharge:
                rechargeRes = getRechargeAction().precharge(company, null, storeGroupId, rule, rechargeAmount);
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的参数值：rechargeType= %s", rechargeType));
        }
        addBalance(rechargeRes);
    }

    public void rechargeByStoreGroupOnce(Integer companyId, String storeGroupId, long rechargeAmount, double unitPrice,
                                         String remarke, RechargeType rechargeType) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeByOrgOnce(%s,%s,%s)", companyId, storeGroupId, rechargeAmount));
        CrmOrganizationEntity company = getCompany(companyId);
        RechargeRuleEntity rule = createTemporaryRule(company, rechargeAmount, unitPrice, remarke);
        RechargeRes rechargeRes = null;
        switch (rechargeType) {
            case Recharge:
                rechargeRes = getRechargeAction().recharge(company, null, storeGroupId, rule, rechargeAmount);
                break;
            case Precharge:
                rechargeRes = getRechargeAction().precharge(company, null, storeGroupId, rule, rechargeAmount);
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的参数值：rechargeType= %s", rechargeType));
        }
        addBalance(rechargeRes);
    }

    /**
     * 门店充值
     *
     * @param companyId      公司
     * @param storeId        门店
     * @param rechargeAmount 金额
     */
    public void rechargeByStore(Integer companyId, Integer storeId, long rechargeAmount, RechargeType rechargeType) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeByStore(%s,%s,%s)", companyId, storeId, rechargeAmount));
        CrmOrganizationEntity company = getCompany(companyId);
        CrmStoreEntity store = getStore(companyId, storeId);
        RechargeRuleEntity rule = getRechargeRule(company, rechargeAmount);
        RechargeRes rechargeRes = null;
        switch (rechargeType) {
            case Recharge:
                rechargeRes = getRechargeAction().recharge(company, store, null, rule, rechargeAmount);
                break;
            case Precharge:
                rechargeRes = getRechargeAction().precharge(company, store, null, rule, rechargeAmount);
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的参数值：rechargeType= %s", rechargeType));
        }
        addBalance(rechargeRes);
    }

    /**
     * 门店一次性充值
     *
     * @param companyId      公司
     * @param storeId        门店
     * @param rechargeAmount 金额
     * @param unitPrice      单价 分
     * @param remarke        备注
     */
    public void rechargeByStoreOnce(Integer companyId, Integer storeId, long rechargeAmount, double unitPrice,
                                    String remarke, RechargeType rechargeType) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeByStoreOnce(%s,%s,%s,%s,%s)", companyId, storeId, rechargeAmount,
                    unitPrice, remarke));
        Optional<CrmOrganizationEntity> company_opt = getBean(CrmOrganizationEntityAction.class).findCompanyById(companyId);
        Preconditions.checkState(company_opt.isPresent());
        CrmOrganizationEntity company = company_opt.get();
        RechargeRuleEntity rule = createTemporaryRule(company, rechargeAmount, unitPrice, remarke);
        CrmStoreEntity store = getStore(companyId, storeId);
        RechargeRes rechargeRes = null;
        switch (rechargeType) {
            case Recharge:
                rechargeRes = getRechargeAction().recharge(company, store, null, rule, rechargeAmount);
                break;
            case Precharge:
                rechargeRes = getRechargeAction().precharge(company, store, null, rule, rechargeAmount);
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的参数值：rechargeType= %s", rechargeType));
        }
        addBalance(rechargeRes);
    }


    private RechargeRuleEntity getRechargeRule(CrmOrganizationEntity company, long rechargeAmount) {
        RechargeRuleSet ruleSet = getBean(RechargeRuleEntityAction.class).loadAllRuleSet();
        Optional<RechargeRuleEntity> rule = ruleSet.getSuitableRule(company, rechargeAmount);
        Preconditions.checkState(rule.isPresent(), "无合适的规则适用于本次扣费");
        return rule.get();
    }

    private RechargeRuleEntity createTemporaryRule(CrmOrganizationEntity company, long rechargeAmount, double unitPrice,
                                                   String remarke) {
        String ruleId = getBean(RechargeRuleEntityAction.class).addTemporaryRule(rechargeAmount * 100 - 100,
                rechargeAmount * 100 + 100,
                unitPrice, company, remarke, null);
        Optional<RechargeRuleEntity> rule = getBean(RechargeRuleEntityAction.class).findTempById(ruleId);
        Preconditions.checkState(rule.isPresent(), "ID=%s 的充值规则不存在...");
        Preconditions.checkState(rule.get().isEnabled());
        return rule.get();
    }

    private RechargeRes rechargeByCompany(Integer companyId, long rechargeAmount, RechargeRuleEntity rule, RechargeType rechargeType) {
        Optional<CrmOrganizationEntity> company_opt = getBean(CrmOrganizationEntityAction.class).findCompanyById(companyId);
        Preconditions.checkState(company_opt.isPresent());
        CrmOrganizationEntity company = company_opt.get();
        RechargeRes rechargeRes = null;
        switch (rechargeType) {
            case Recharge:
                rechargeRes = getRechargeAction().recharge(company, null, null, rule, rechargeAmount);
                break;
            case Precharge:
                rechargeRes = getRechargeAction().precharge(company, null, null, rule, rechargeAmount);
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的参数值：rechargeType= %s", rechargeType));
        }
        return rechargeRes;
    }

}
