package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.smsgateway.entity.*;
import com.legooframework.model.smsgateway.mvc.RechargeReqDto;
import com.legooframework.model.smsprovider.entity.SMSProxyEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SmsAnyListenerService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsAnyListenerService.class);

    /**
     * 充值缴费等相关请求处理
     * 监听队列 CHANNEL_SMS_CHARGE = "channel_sms_charge"
     *
     * @param user    当前用户
     * @param action  动作
     * @param payload 有效负载
     * @return Message 消息
     */
    public Message<?> billingAndDeduction(@Header(name = "user") UserAuthorEntity user,
                                          @Header(name = "action") String action,
                                          @Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("billingAndDeduction(user:%s,action:%s,payload:....)", user.getId(), action));
        LoginContextHolder.setCtx(user.toLoginContext());
        try {
            if (StringUtils.equals("recharge", action)) {// 充值行为
                this.recharge((RechargeReqDto) payload);
            } else if (StringUtils.equals("deduction", action)) { // 计费行为
                String sendBathcNo = (String) payload;
                this.deduction(sendBathcNo);
                return MessageBuilder.withPayload(new Object()).build();
            } else if (StringUtils.equals("writeOff", action)) { // 退款行为
//                String sendBatchNo = (String) payload;
//                Optional<List<SendMsg4ReimburseEntity>> list_opt = getBean(SendMsg4ReimburseEntityAction.class)
//                        .loadBySendBatchNo(sendBatchNo);
//                list_opt.ifPresent(this::reimburseAction);
            } else {
                throw new RuntimeException(String.format("非法的请求参数：%s", action));
            }
            return MessageBuilder.withPayload("ok").build();
        } catch (Exception e) {
            logger.error("billingAndDeduction(%s) has error", e);
            return MessageBuilder.withPayload(e).build();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * OOXX
     *
     * @return 行走的银行
     */
    void deduction(String sendBathcNo) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        SendMsg4InitEntityAction.MsgTransBatch batchInfo = sendMsg4InitEntityAction.loadBatchInfo(sendBathcNo);
        if (batchInfo.isBilling()) return;
        try {
            getBean(SendMsg4InitEntityAction.class).updateWxMsg4SendByBatchNo(batchInfo);
        } catch (Exception e) {
            logger.error(String.format("updateWxMsg4SendByBatchNo(%s) has error...,rollback 事务", sendBathcNo), e);
        }
        StoEntity store = getStore(batchInfo.getStoreId());
        Optional<List<SendMsg4DeductionEntity>> deduction_sms_list = sendMsg4InitEntityAction
                .loadSmsMsg4SendByBatchNo(batchInfo);
        if (!deduction_sms_list.isPresent()) {
            sendMsg4InitEntityAction.finishedBill(batchInfo);
        } else {
            TransactionStatus ts = startTx(null);
            try {
                RechargeBalanceAgg balanceAgg = rechargeBalanceEntityAction.loadOrderEnabledByStore(store);
                balanceAgg.deduction(store, batchInfo.getBatchNo(), deduction_sms_list.get());
                sendMsg4InitEntityAction.batchUpdateMsg4Deductions(balanceAgg.getDeductionSmses());
                balanceAgg.getChargeDetails().ifPresent(x -> deductionDetailEntityAction.batchInsert(x));
                balanceAgg.getDeductionBalances().ifPresent(x -> rechargeBalanceEntityAction.batchUpdateBalance(x));
                commitTx(ts);
            } catch (Exception e) {
                logger.error(String.format("deduction(%s) has error...,rollback 事务", sendBathcNo), e);
                rollbackTx(ts);
                String msg = e.getMessage();
                if (msg.length() > 512) msg = msg.substring(0, 512);
                sendMsg4InitEntityAction.batchFailMsg4Deductions(deduction_sms_list.get(), msg);
            } finally {
                sendMsg4InitEntityAction.finishedBill(batchInfo);
            }
        }
    }

    /**
     * 系统充值相关接口
     *
     * @param rechargeDto OOX
     * @throws Exception 异常或
     */
    private void recharge(RechargeReqDto rechargeDto) throws Exception {
        TransactionStatus ts = startTx(null);
        try {
            if (rechargeDto.isFreeCharge()) {
                getBean(SMSRechargeService.class).freecharge(rechargeDto.getCompanyId(),
                        rechargeDto.isStoreGroupRange() ? rechargeDto.getStoreIds() : null,
                        rechargeDto.isStoreRange() ? rechargeDto.getStoreId() : null,
                        rechargeDto.getTotalQuantity());
            } else {
                if (rechargeDto.hasUnitPrice()) {
                    if (rechargeDto.isStoreRange()) {
                        getBean(SMSRechargeService.class).rechargeByStoreOnce(rechargeDto.getCompanyId(),
                                rechargeDto.getStoreId(),
                                rechargeDto.getRechargeAmount(), rechargeDto.getUnitPrice(), rechargeDto.getRemarke(),
                                rechargeDto.getRechargeType());
                    } else if (rechargeDto.isStoreGroupRange()) {
                        getBean(SMSRechargeService.class).rechargeByStoreGroupOnce(rechargeDto.getCompanyId(),
                                rechargeDto.getStoreIds(),
                                rechargeDto.getRechargeAmount(), rechargeDto.getUnitPrice(), rechargeDto.getRemarke(),
                                rechargeDto.getRechargeType());
                    } else {
                        getBean(SMSRechargeService.class).rechargeByCompanyOnce(rechargeDto.getCompanyId(),
                                rechargeDto.getRechargeAmount(), rechargeDto.getUnitPrice(), rechargeDto.getRemarke(),
                                rechargeDto.getRechargeType());
                    }
                } else {
                    if (rechargeDto.isStoreRange()) {
                        getBean(SMSRechargeService.class).rechargeByStore(rechargeDto.getCompanyId(), rechargeDto.getStoreId(),
                                rechargeDto.getRechargeAmount(), rechargeDto.getRechargeType());
                    } else if (rechargeDto.isStoreGroupRange()) {
                        getBean(SMSRechargeService.class).rechargeByStoreGroup(rechargeDto.getCompanyId(),
                                rechargeDto.getStoreIds(),
                                rechargeDto.getRechargeAmount(), rechargeDto.getRechargeType());
                    } else {
                        getBean(SMSRechargeService.class).rechargeByCompany(rechargeDto.getCompanyId(),
                                rechargeDto.getRechargeAmount(), rechargeDto.getRechargeType());
                    }
                }
            }
            commitTx(ts);
        } catch (Exception e) {
            logger.error(String.format("recharge(%s) has error...", rechargeDto), e);
            rollbackTx(ts);
            throw e;
        }
    }

    /**
     * @param payload 发送监听
     */
    public void listen4SendSMS(@Payload Map<String, Object> payload) {
        String smsId = MapUtils.getString(payload, "id");
        String mixed = MapUtils.getString(payload, "mixed");
        String context = MapUtils.getString(payload, "ctx");
        SendMsg4SendEntity result = getBean(SMSProxyEntityAction.class).sendSingleSms(smsId, mixed, context);
        getBean(SendMsg4InitEntityAction.class).updateSendState(result);
    }

    /**
     * 状态回查监听
     */
    public void listen4SyncSMS() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<List<String>> smsIds = getBean(SendMsg4InitEntityAction.class).loadNeedSyncStateSmsIds();
        if (!smsIds.isPresent()) return;
        try {
            if (smsIds.get().size() <= 128) {
                Optional<List<SendMsg4FinalEntity>> res_states = getBean(SMSProxyEntityAction.class).syncSmsState(smsIds.get());
                res_states.ifPresent($it -> getBean(SendMsg4InitEntityAction.class).updateFinalState($it));
            } else {
                List<List<String>> list_smsIds = Lists.partition(smsIds.get(), 128);
                List<CompletableFuture<Void>> cfs = Lists.newArrayListWithCapacity(list_smsIds.size());
                for (List<String> list : list_smsIds) {
                    CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> getBean(SMSProxyEntityAction.class).syncSmsState(list))
                            .thenAccept(res_states -> {
                                LoginContextHolder.setIfNotExitsAnonymousCtx();
                                try {
                                    res_states.ifPresent($it -> getBean(SendMsg4InitEntityAction.class).updateFinalState($it));
                                } finally {
                                    LoginContextHolder.clear();
                                }
                            });
                    cfs.add(cf);
                }
                if (CollectionUtils.isNotEmpty(cfs)) {
                    CompletableFuture.allOf(cfs.toArray(new CompletableFuture[]{})).join();
                }
            }
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 同步黑名单
     */
    public void syncBlackList() {
        LoginContextHolder.setAnonymousCtx();
        try {
            DateTime[] dateTimes = getBean(SMSBlackListEntityAction.class).getLastSyncTime();
            Optional<List<Integer>> companyIds = getBean(SMSBlackListEntityAction.class).loadSMSCompanys();
            if (!companyIds.isPresent()) return;
            Map<String, Object> params = Maps.newHashMap();
            params.put("dateStart", dateTimes[0].toString("yyyy-MM-dd HH:mm:ss"));
            params.put("dateEnd", dateTimes[1].toString("yyyy-MM-dd HH:mm:ss"));
            params.put("companyIds", StringUtils.join(companyIds.get(), ','));
            Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve().bodyToMono(String.class);
            String http_return = mono.block(Duration.ofSeconds(30));
            if (logger.isDebugEnabled())
                logger.debug(String.format("URL=%s,params=%s,return %s", "", params, http_return));
            Optional<JsonElement> payload_opt = WebUtils.parseJson(http_return);
            if (!payload_opt.isPresent()) return;
            String payload_data = payload_opt.get().getAsString();
            String[] args = StringUtils.splitByWholeSeparator(payload_data, "|||");
            List<SMSBlackListEntity> instance_list = Lists.newArrayListWithCapacity(args.length);
            for (String str : args) {
                String[] arg = StringUtils.split(str, '|');
                instance_list.add(SMSBlackListEntity.disableInstance(Integer.valueOf(arg[0]),
                        Integer.valueOf(arg[1]), arg[2]));
            }
            getBean(SMSBlackListEntityAction.class).diabled(instance_list);
        } finally {
            LoginContextHolder.clear();
        }
    }

    void reimburseAction(Collection<SendMsg4ReimburseEntity> reimburses) {
        Map<String, Long> total = Maps.newConcurrentMap();
        reimburses.forEach(x -> {
            long size = MapUtils.getIntValue(total, x.getSendBatchNo(), 0) + x.getSmsCount();
            total.put(x.getSendBatchNo(), size);
        });

        if (logger.isDebugEnabled())
            logger.debug(String.format("writeOffService() total : %s", total));
        Multimap<String, DeductionDetailEntity> chargeDetail_map = getBean(DeductionDetailEntityAction.class)
                .loadBySmsBatchNos(total.keySet());
        List<String> balanceIds = chargeDetail_map.entries().stream().map(x -> x.getValue().getBalanceId())
                .collect(Collectors.toList());
        List<RechargeBalanceEntity> balances = getBean(RechargeBalanceEntityAction.class).loadByIds(balanceIds);
        final List<DeductionDetailEntity> change_detail = Lists.newArrayList();
        final List<RechargeBalanceEntity> change_balance = Lists.newArrayList();
        final List<RechargeDetailEntity> change_recharge = Lists.newArrayList();
        total.forEach((k, v) -> {
            long size = v;
            Optional<RechargeBalanceEntity> balance;
            Collection<DeductionDetailEntity> details = chargeDetail_map.get(k);
            for (DeductionDetailEntity $it : details) {
                if ($it.getWriteOffNum() == 0L) continue;
                balance = balances.stream().filter(x -> x.getId().equals($it.getBalanceId())).findFirst();
                Preconditions.checkState(balance.isPresent());
                if ($it.getWriteOffNum() >= size) {
                    $it.reimburse(size);
                    change_detail.add($it);
                    balance.get().addBalance((int) size);
                    change_balance.add(balance.get());
                    change_recharge.add(RechargeDetailEntity.writeOff(balance.get(), (int) size));
                    break;
                } else {
                    long _balance = $it.getWriteOffNum();
                    $it.reimburse(_balance);
                    change_detail.add($it);
                    balance.get().addBalance((int) _balance);
                    change_balance.add(balance.get());
                    change_recharge.add(RechargeDetailEntity.writeOff(balance.get(), (int) _balance));
                    size = size - $it.getWriteOffNum();
                }
            }
        });
        TransactionStatus ts = startTx(null);
        try {
            getBean(SendMsg4ReimburseEntityAction.class).batchReimburse(reimburses);
            getBean(DeductionDetailEntityAction.class).batchWriteOff(change_detail);
            getBean(RechargeBalanceEntityAction.class).batchUpdateBalance(change_balance);
            getBean(RechargeDetailEntityAction.class).batchWriteOff(change_recharge);
            commitTx(ts);
        } catch (Exception e) {
            logger.error(String.format("[Event-Driver]事件发布失败.....%s", "writeOff"), e);
            rollbackTx(ts);
            throw e;
        }
    }

    private SendMsg4InitEntityAction sendMsg4InitEntityAction;
    private RechargeBalanceEntityAction rechargeBalanceEntityAction;
    private DeductionDetailEntityAction deductionDetailEntityAction;

    public void setDeductionDetailEntityAction(DeductionDetailEntityAction deductionDetailEntityAction) {
        this.deductionDetailEntityAction = deductionDetailEntityAction;
    }

    public void setSendMsg4InitEntityAction(SendMsg4InitEntityAction sendMsg4InitEntityAction) {
        this.sendMsg4InitEntityAction = sendMsg4InitEntityAction;
    }

    public void setRechargeBalanceEntityAction(RechargeBalanceEntityAction rechargeBalanceEntityAction) {
        this.rechargeBalanceEntityAction = rechargeBalanceEntityAction;
    }
}
