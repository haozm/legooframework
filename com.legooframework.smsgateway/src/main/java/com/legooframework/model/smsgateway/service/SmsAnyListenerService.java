package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.smsgateway.entity.*;
import com.legooframework.model.smsgateway.mvc.RechargeReqDto;
import com.legooframework.model.smsprovider.entity.SMSProxyEntityAction;
import com.legooframework.model.smsprovider.entity.SmsStateDto;
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
     * @param action  动作
     * @param payload 有效负载
     * @return Message 消息
     */
    public Message<?> billingAndDeduction(@Header(name = "action") String action, @Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("billingAndDeduction(action:%s,payload:....)", action));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            if (StringUtils.equals("recharge", action)) {// 充值行为
                this.recharge((RechargeReqDto) payload);
            } else if (StringUtils.equals("deduction", action)) { // 计费行为
                this.deduction((String) payload);
            } else if (StringUtils.equals("reimburse", action)) { // 退款行为
                ReimburseResDto reimburseDto = (ReimburseResDto) payload;
                this.reimburse(reimburseDto);
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

    public void deductionJob() {
        Optional<List<String>> optional = msgTransportBatchEntityAction.load4Deduction();
        if (!optional.isPresent()) return;
        optional.get().forEach(s -> {
            Message<String> message = MessageBuilder.withPayload(s).setReplyChannelName("nullChannel")
                    .setHeader("action", "deduction").build();
            getMessagingTemplate().send(CHANNEL_SMS_BILLING, message);
        });
    }

    /**
     * OOXX
     */
    void deduction(String batchNo) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        MsgTransportBatchEntity transportBatch = msgTransportBatchEntityAction.loadByBatchNo(batchNo);
        if (transportBatch.isBilling()) return;
        try {
            int size = sendMsgStateEntityAction.batchUpdate4WxMsgByBatchNo(transportBatch);
            if (size > 0) {
                Message<MsgTransportBatchEntity> message = MessageBuilder.withPayload(transportBatch).build();
                getMessagingTemplate().send("channel_wx_sending", message);
            }
        } catch (Exception e) {
            logger.error(String.format("updateWxMsg4SendByBatchNo(%s) has error...,rollback 事务", batchNo), e);
        }
        StoEntity store = getStore(transportBatch.getStoreId());
        Optional<List<SendMsg4DeductionEntity>> deduction_sms_list = sendMsgStateEntityAction
                .loadSmsMsg4SendByBatchNo(transportBatch);
        if (!deduction_sms_list.isPresent()) {
            msgTransportBatchEntityAction.finishBilling(transportBatch);
        } else {
            TransactionStatus ts = startTx(null);
            try {
                RechargeBalanceAgg balanceAgg = rechargeBalanceEntityAction.loadOrder4RechargeByStore(store);
                balanceAgg.deduction(transportBatch, deduction_sms_list.get());
                if (logger.isDebugEnabled())
                    logger.debug(String.format("deduction() is %s", balanceAgg));
                sendMsgStateEntityAction.batchUpdate4Deduction(balanceAgg.getDeductionSmses());
                balanceAgg.getChargeDetails().ifPresent(x -> deductionDetailEntityAction.batchInsert(x));
                balanceAgg.getDeductionBalances().ifPresent(x -> rechargeBalanceEntityAction.batchUpdateBalance(x));
                commitTx(ts);
            } catch (Exception e) {
                logger.error(String.format("deduction(%s) has error...,rollback 事务", batchNo), e);
                rollbackTx(ts);
                String msg = e.getMessage();
                if (msg.length() > 512) msg = msg.substring(0, 512);
                sendMsgStateEntityAction.batchUpdate4ErrorDeduction(deduction_sms_list.get(), msg);
            } finally {
                msgTransportBatchEntityAction.finishBilling(transportBatch);
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
    public void sendSmsEndpoint(@Payload Map<String, Object> payload) {
        String smsId = MapUtils.getString(payload, "id");
        String mixed = MapUtils.getString(payload, "mixed");
        String context = MapUtils.getString(payload, "ctx");
        SendMsg4SendEntity result = getBean(SMSProxyEntityAction.class).sendSingleSms(smsId, mixed, context);
        getBean(SendMsgStateEntityAction.class).batchUpdate4SendState(Lists.newArrayList(result));
    }

    private void handleReplyState(List<SmsStateDto> smsStateDtos) {
        if (CollectionUtils.isEmpty(smsStateDtos)) return;
        List<SmsStateDto> error_dto_list = smsStateDtos.stream().filter(SmsStateDto::hasError)
                .collect(Collectors.toList());
        List<SmsStateDto> final_dto_list = smsStateDtos.stream().filter(SmsStateDto::hasFinalState)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(error_dto_list)) {
            List<SendMsg4SendEntity> send_list = error_dto_list.stream()
                    .map(x -> SendMsg4SendEntity.createSMS4SendError(x.getSmsId(), x.getStateDesc()))
                    .collect(Collectors.toList());
            sendMsgStateEntityAction.batchUpdate4SendState(send_list);
        }
        if (CollectionUtils.isNotEmpty(final_dto_list)) {
            List<SendMsg4FinalEntity> final_list = final_dto_list.stream()
                    .map(x -> SendMsg4FinalEntity.create(x.getSmsId(), x.getStateCode(), x.getStateDate(),
                            x.getStateDesc())).collect(Collectors.toList());
            sendMsgStateEntityAction.batchUpdate4FinalState(final_list);
        }
    }

    public void reimburseJob() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            Optional<List<ReimburseResDto>> reimburseResDtos = reimburseEntityAction.loadUnReimburseDto();
            if (!reimburseResDtos.isPresent()) return;
            for (ReimburseResDto $it : reimburseResDtos.get()) {
                Message<ReimburseResDto> message = MessageBuilder.withPayload($it).setReplyChannelName("nullChannel")
                        .setHeader("action", "reimburse").build();
                getMessagingTemplate().send(CHANNEL_SMS_BILLING, message);
            }
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 状态回查监听
     */
    public void syncStateJob() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<List<String>> smsIds = sendMsgStateEntityAction.loadNeedSyncStateSmsIds();
        if (!smsIds.isPresent()) return;
        try {
            if (smsIds.get().size() <= 128) {
                Optional<List<SmsStateDto>> res_states = getBean(SMSProxyEntityAction.class).syncSmsState(smsIds.get());
                res_states.ifPresent(this::handleReplyState);
            } else {
                List<List<String>> list_smsIds = Lists.partition(smsIds.get(), 128);
                List<CompletableFuture<Void>> cfs = Lists.newArrayListWithCapacity(list_smsIds.size());
                for (List<String> list : list_smsIds) {
                    CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> getBean(SMSProxyEntityAction.class).syncSmsState(list))
                            .thenAccept(res_states -> {
                                LoginContextHolder.setIfNotExitsAnonymousCtx();
                                try {
                                    res_states.ifPresent(this::handleReplyState);
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

    public void sendWxEndpoint(@Payload MsgTransportBatchEntity transportBatch) {
        if (logger.isDebugEnabled())
            logger.debug("autoSendWxMsgJob() .................. start");
        LoginContextHolder.setAnonymousCtx();
        try {
            wechatMessageEntityAction.sendWxMessage(transportBatch);
        } finally {
            LoginContextHolder.clear();
        }
        if (logger.isDebugEnabled())
            logger.debug("autoSendWxMsgJob() .................. end");
    }


    private void reimburse(ReimburseResDto reimburse) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("reimburseAction()  %s", reimburse));
        LoginContextHolder.setAnonymousCtx();
        StoEntity store = getStore(reimburse.getStoreId());
        ReimburseBalanceAgg reimburseAgg = rechargeBalanceEntityAction.loadOrder4ReimburseByStore(store);
        reimburseAgg.reimburse(reimburse);
        if (logger.isDebugEnabled())
            logger.debug(String.format("reimburse() is %s", reimburseAgg));
        TransactionStatus ts = startTx(null);
        try {
            reimburseAgg.getRechargeDetail().ifPresent(x -> rechargeDetailEntityAction.batchReimburse(Lists.newArrayList(x)));
            reimburseAgg.getBalance().ifPresent(x -> rechargeBalanceEntityAction.batchUpdateBalance(Lists.newArrayList(x)));
            if (reimburseAgg.getBalance().isPresent()) {
                reimburseEntityAction.updateReimburseState(reimburse);
            }
            LoginContextHolder.clear();
            commitTx(ts);
        } catch (Exception e) {
            logger.error(String.format("reimburseAction() has error..%s", reimburse), e);
            rollbackTx(ts);
            LoginContextHolder.clear();
            throw e;
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

//    void reimburseAction(Collection<SendMsg4ReimburseEntity> reimburses) {
//        Map<String, Long> total = Maps.newConcurrentMap();
////        reimburses.forEach(x -> {
////            long size = MapUtils.getIntValue(total, x.getSendBatchNo(), 0) + x.getSmsCount();
////            total.put(x.getSendBatchNo(), size);
////        });
//
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("writeOffService() total : %s", total));
//        Multimap<String, DeductionDetailEntity> chargeDetail_map = getBean(DeductionDetailEntityAction.class)
//                .loadBySmsBatchNos(total.keySet());
//        List<String> balanceIds = chargeDetail_map.entries().stream().map(x -> x.getValue().getBalanceId())
//                .collect(Collectors.toList());
//        List<RechargeBalanceEntity> balances = getBean(RechargeBalanceEntityAction.class).loadByIds(balanceIds);
//        final List<DeductionDetailEntity> change_detail = Lists.newArrayList();
//        final List<RechargeBalanceEntity> change_balance = Lists.newArrayList();
//        final List<RechargeDetailEntity> change_recharge = Lists.newArrayList();
//        total.forEach((k, v) -> {
//            long size = v;
//            Optional<RechargeBalanceEntity> balance;
//            Collection<DeductionDetailEntity> details = chargeDetail_map.get(k);
//            for (DeductionDetailEntity $it : details) {
//                if ($it.getWriteOffNum() == 0L) continue;
//                balance = balances.stream().filter(x -> x.getId().equals($it.getBalanceId())).findFirst();
//                Preconditions.checkState(balance.isPresent());
//                if ($it.getWriteOffNum() >= size) {
//                    $it.reimburse(size);
//                    change_detail.add($it);
//                    balance.get().addBalance((int) size);
//                    change_balance.add(balance.get());
//                    change_recharge.add(RechargeDetailEntity.writeOff(balance.get(), (int) size));
//                    break;
//                } else {
//                    long _balance = $it.getWriteOffNum();
//                    $it.reimburse(_balance);
//                    change_detail.add($it);
//                    balance.get().addBalance((int) _balance);
//                    change_balance.add(balance.get());
//                    change_recharge.add(RechargeDetailEntity.writeOff(balance.get(), (int) _balance));
//                    size = size - $it.getWriteOffNum();
//                }
//            }
//        });
//        TransactionStatus ts = startTx(null);
//        try {
//            getBean(SendMsg4ReimburseEntityAction.class).batchReimburse(reimburses);
//            getBean(DeductionDetailEntityAction.class).batchWriteOff(change_detail);
//            getBean(RechargeBalanceEntityAction.class).batchUpdateBalance(change_balance);
//            getBean(RechargeDetailEntityAction.class).batchWriteOff(change_recharge);
//            commitTx(ts);
//        } catch (Exception e) {
//            logger.error(String.format("[Event-Driver]事件发布失败.....%s", "writeOff"), e);
//            rollbackTx(ts);
//            throw e;
//        }
//    }

}
