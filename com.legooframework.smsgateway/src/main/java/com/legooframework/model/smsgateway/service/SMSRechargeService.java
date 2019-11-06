package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.smsgateway.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SMSRechargeService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SMSRechargeService.class);

    /**
     * 公司充值 巴拉巴拉
     *
     * @param companyId      公司ID
     * @param rechargeAmount 充值金额
     */
    public void rechargeByCompany(Integer companyId, long rechargeAmount, RechargeType rechargeType) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeByCompany(%s,%s)", companyId, rechargeAmount));
        OrgEntity company = getCompany(companyId);
        RechargeRuleEntity rule = getRechargeRule(company, rechargeAmount);
        RechargeRes rechargeRes = rechargeByCompany(companyId, rechargeAmount, rule, rechargeType);
        addBalance(rechargeRes);
    }

    /**
     * 免费充值
     *
     * @param companyId     公司ID
     * @param storeGroupId  组织中间ID
     * @param storId        门店ID
     * @param totalQuantity 总量
     */
    void freecharge(Integer companyId, String storeGroupId, Integer storId, int totalQuantity) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("freecharge(%s,%s,%s,%s)", companyId, storeGroupId, storId, totalQuantity));
        OrgEntity company = getCompany(companyId);
        StoEntity store = storId == null ? null : getStore(companyId, storId);
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
        OrgEntity company = getCompany(companyId);
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
        OrgEntity company = getCompany(companyId);
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
        OrgEntity company = getCompany(companyId);
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
        OrgEntity company = getCompany(companyId);
        StoEntity store = getStore(companyId, storeId);
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
        OrgEntity company = getBean(OrgEntityAction.class).loadComById(companyId);
        RechargeRuleEntity rule = createTemporaryRule(company, rechargeAmount, unitPrice, remarke);
        StoEntity store = getStore(companyId, storeId);
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

    private RechargeRuleEntity getRechargeRule(OrgEntity company, long rechargeAmount) {
        RechargeRuleSet ruleSet = getBean(RechargeRuleEntityAction.class).loadAllRuleSet();
        Optional<RechargeRuleEntity> rule = ruleSet.getSuitableRule(company, rechargeAmount);
        Preconditions.checkState(rule.isPresent(), "无合适的规则适用于本次扣费");
        return rule.get();
    }

    private RechargeRuleEntity createTemporaryRule(OrgEntity company, long rechargeAmount, double unitPrice,
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
        OrgEntity company = getCompany(companyId);
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

    /**
     * 退款服务
     */
    public void batchWriteOffService() {
//        Optional<List<SMSTransportLogEntity>> optional = getBean(SMSTransportLogEntityAction.class).loadSms4WriteOff();
//        optional.ifPresent(xs -> {
//            Message<List<SMSTransportLogEntity>> msg_request = MessageBuilder.withPayload(optional.get())
//                    .setHeader("user", LoginContextHolder.getAnonymousCtx())
//                    .setHeader("action", "writeOff")
//                    .build();
//            Message<?> message_rsp = getMessagingTemplate().sendAndReceive("channel_sms_balance", msg_request);
//            if (message_rsp.getPayload() instanceof Exception) {
//                logger.error(String.format("batchWriteOffService() has error...", (Exception) message_rsp.getPayload()));
//            }
//        });
    }


}
