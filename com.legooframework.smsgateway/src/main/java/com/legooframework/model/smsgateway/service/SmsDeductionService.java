package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.smsgateway.entity.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class SmsDeductionService extends SMSService {

    /**
     * 计费服务
     *
     * @param smses
     * @param businessType
     * @param store
     */
    public String charging(List<SMSEntity> smses, KvDictEntity businessType, final CrmStoreEntity store,
                           boolean isAuto, String smsContext) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(smses), "待发送的短信数不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(smsContext), "短信模板摘要不可以为空...");
        final String sms_batch_no = String.format("%s_%s_%s", store.getCompanyId(), store.getId(),
                CommonsUtils.randomId(12).toUpperCase());
        final SMSSettingEntity sms_setting = getBean(SMSSettingEntityAction.class).loadByStore(store);
        final SMSSendRuleEntity sendRule = getBean(SMSSendRuleEntityAction.class).findByType(businessType);
        if (sendRule.isMarketChannel()) {
            smses.forEach(sms -> sms.addPrefix(sms_setting.getSmsPrefix()));
        } else if (sendRule.isTradeChannel()) {
            smses.forEach(sms -> sms.addPrefixAndSuffix(sms_setting.getSmsPrefix()));
        }
        final long beused_num = smses.stream().mapToInt(SMSEntity::getSmsNum).sum();
        String sms_batch_id;
        if (isAuto) {
            sms_batch_id = getSummaryAction().insertAuto(store, sendRule, sms_batch_no, beused_num, smsContext);
        } else {
            sms_batch_id = getSummaryAction().insertManual(store, sendRule, sms_batch_no, beused_num, smsContext);
        }
        if (!sendRule.isFreeSend()) {
            RechargeBalanceList balancesList = getBean(RechargeBalanceEntityAction.class).loadOrderEnabledByStore(store);
            List<ChargeDetailEntity> chargeDetails = balancesList.deduction(store, sms_batch_no, beused_num);
            List<RechargeBalanceEntity> rechargeBalances = balancesList.getDeductionList();
            getBean(ChargeDetailEntityAction.class).batchInsert(chargeDetails);
            getBean(RechargeBalanceEntityAction.class).batchUpdateBalance(rechargeBalances);
        }
        List<SMSTransportLogEntity> transportLogs = smses.stream()
                .map(x -> new SMSTransportLogEntity(store, x, sms_batch_no, sendRule)).collect(Collectors.toList());
        getBean(SMSTransportLogEntityAction.class).batchAdd4Init(transportLogs);
        return sms_batch_id;
    }

}
