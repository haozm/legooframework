package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.legooframework.model.commons.entity.CommunicationChannel;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.*;
import com.legooframework.model.smsgateway.filter.SmsSendInterceptor;
import com.legooframework.model.smsgateway.mvc.DeductionReqDto;
import com.legooframework.model.smsgateway.mvc.RechargeReqDto;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsprovider.entity.SMSProviderEntity;
import com.legooframework.model.smsprovider.entity.SMSProviderEntityAction;
import com.legooframework.model.smsresult.entity.SMSSendTransportProtocol;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmsIntegrationService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsIntegrationService.class);

    /**
     * 充值缴费等相关请求处理
     * 监听队列 CHANNEL_SMS_CHARGE = "channel_sms_charge"
     *
     * @param user    当前用户
     * @param action  动作
     * @param payload 有效负载
     * @return Message 消息
     */
    public Message<?> billingAndSettlement(@Header(name = "user") LoginContext user, @Header(name = "action") String action,
                                           @Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("billingAndSettlement(user:%s,action:%s,payload:....)", user.getLoginId(),
                    action));
        LoginContextHolder.setCtx(user);
        try {
            if (StringUtils.equals("recharge", action)) {// 充值行为
                RechargeReqDto rechargeDto = (RechargeReqDto) payload;
                this.recharge(rechargeDto);
            } else if (StringUtils.equals("charge", action)) { // 计费统计
                DeductionReqDto deduction = (DeductionReqDto) payload;
                String batch_no = this.charge(user, deduction.getSmses(), deduction.getBusinessType(), deduction.getStore(),
                        deduction.getSmsContext());
                return MessageBuilder.withPayload(batch_no).build();
            } else if (StringUtils.equals("writeOff", action)) { // 退款行为
                String sendBatchNo = (String) payload;
                Optional<List<SendMsg4ReimburseEntity>> list_opt = getBean(SendMsg4ReimburseEntityAction.class)
                        .loadBySendBatchNo(sendBatchNo);
                list_opt.ifPresent(this::reimburseAction);
            } else {
                throw new RuntimeException(String.format("非法的请求参数：%s", action));
            }
            return MessageBuilder.withPayload("ok").build();
        } catch (Exception e) {
            logger.error("billingAndSettlement(%s) has error", e);
            return MessageBuilder.withPayload(e).build();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * @param user 我的而用户
     * @return 行走的银行
     */
    private String charge(LoginContext user, List<SMSEntity> smses, BusinessType businessType, CrmStoreEntity store,
                          String smsCtx) {
        TransactionStatus ts = startTx(null);
        String summaryId;
        try {
            summaryId = this.chargingAction(smses, businessType, store, smsCtx);
            commitTx(ts);
        } catch (Exception e) {
            logger.error(String.format("charge(%s,businessType:%s) has error...,rollback 事务", store, businessType), e);
            rollbackTx(ts);
            throw e;
        }
        Optional<ChargeSummaryEntity> chargeSummary = getBean(ChargeSummaryEntityAction.class).findById(summaryId);
        Preconditions.checkState(chargeSummary.isPresent(), "数据异常: Id=%s 对应的充值计费不存在...", summaryId);

        // 按照批次号获取短信送入发送通道
        Optional<List<SendMsg4SendEntity>> send_sms_list_opt = getBean(SendMsg4SendEntityAction.class)
                .load4Sending(chargeSummary.get(), CommunicationChannel.SMS);
        Optional<List<SendMsg4SendEntity>> send_wx_list_opt = getBean(SendMsg4SendEntityAction.class)
                .load4Sending(chargeSummary.get(), CommunicationChannel.WEIXIN);
        if (send_sms_list_opt.isPresent()) {
            List<SendMsg4SendEntity> send_sms_list = send_sms_list_opt.get();
            getBean("smsBeforeSendInterceptor", SmsSendInterceptor.class).filter(send_sms_list);
            List<SendMsg4SendEntity> errors_list = send_sms_list.stream().filter(SendMsg4SendEntity::isError)
                    .collect(Collectors.toList());
            List<SendMsg4SendEntity> send_list = send_sms_list.stream().filter(SendMsg4SendEntity::isSending)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(errors_list)) {
                Message<List<SendMsg4SendEntity>> msg_request = MessageBuilder.withPayload(errors_list)
                        .setHeader("action", "writeOff")
                        .setHeader("sendBatchNo", chargeSummary.get().getSmsBatchNo())
                        .setHeader("user", user).build();
                getMessagingTemplate().send(BundleService.CHANNEL_SMS_SENDED, msg_request);
            }

            if (CollectionUtils.isEmpty(send_list)) return chargeSummary.get().getSmsBatchNo();
            if (send_list.size() <= 200) {
                Message<List<SendMsg4SendEntity>> message = MessageBuilder.withPayload(send_list)
                        .setHeader("user", user).build();
                getMessagingTemplate().send(BundleService.CHANNEL_SMS_SENDING, message);
            } else {
                List<List<SendMsg4SendEntity>> list_list = Lists.partition(send_list, 200);
                List<Message<List<SendMsg4SendEntity>>> messages = list_list.stream().map(x -> MessageBuilder.withPayload(x)
                        .setHeader("user", user).build())
                        .collect(Collectors.toList());
                messages.forEach(msg -> getMessagingTemplate().send(BundleService.CHANNEL_SMS_SENDING, msg));
            }
        }
        // 发送微信逻辑
        if (send_wx_list_opt.isPresent()) {
            List<SendMsg4SendEntity> send_wx_list = send_wx_list_opt.get();
            List<SendMsg4SendEntity> error_list = send_wx_list.stream().filter(x -> !x.isEnbaled())
                    .filter(x -> !x.hasWeixin()).collect(Collectors.toList());
            List<SendMsg4SendEntity> sending_list = send_wx_list.stream().filter(SendMsg4InitEntity::isEnbaled)
                    .filter(SendMsg4InitEntity::hasWeixin).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(error_list)) {
                error_list.forEach(x -> x.errorBySending("微信发送信息异常..."));
                Message<List<SendMsg4SendEntity>> msg_request = MessageBuilder.withPayload(error_list)
                        .setHeader("user", LoginContextHolder.get()).build();
                getMessagingTemplate().send(BundleService.CHANNEL_SMS_SENDED, msg_request);
            }
            if (CollectionUtils.isNotEmpty(sending_list)) {
                List<WechatMessageEntity> wechatMessages = Lists.newArrayListWithCapacity(sending_list.size());
                final String batchNo = chargeSummary.get().getSmsBatchNo();
                sending_list.forEach(msg -> wechatMessages.add(WechatMessageEntity
                        .createMessage4Txt(msg.getSms().getContent(), msg.getSms().getWeixinId(), batchNo,
                                msg.getSms().getDeviceId(), msg.getStoreId(), UUID.randomUUID().toString(), 1,
                                msg.getCreator(), msg.getTenantId())));
                getBean(WechatMessageEntityAction.class).batchSend(wechatMessages);
                sending_list.forEach(SendMsg4SendEntity::finshedByWechat);
                Message<List<SendMsg4SendEntity>> msg_request = MessageBuilder.withPayload(sending_list)
                        .setHeader("user", LoginContextHolder.get()).build();
                getMessagingTemplate().send(BundleService.CHANNEL_SMS_SENDED, msg_request);
            }
        }
        return chargeSummary.get().getSmsBatchNo();
    }


    /**
     * 短信发送计费服务
     *
     * @param smses        待发送的短信
     * @param businessType 业务类型
     * @param store        所属门店
     * @param smsContext   短信摘要
     */
    private String chargingAction(List<SMSEntity> smses, final BusinessType businessType, final CrmStoreEntity store,
                                  String smsContext) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(smses), "待发送的短信数不可以为空...");
        LoginContext user = LoginContextHolder.get();
        final String sms_batch_no = String.format("%s_%s_%s", store.getCompanyId(), store.getId(),
                CommonsUtils.randomId(12));
        List<SMSEntity> wx_msgs = smses.stream().filter(SMSEntity::isWxMsg).collect(Collectors.toList());
        List<SMSEntity> sms_msgs = smses.stream().filter(SMSEntity::isSMSMsg).collect(Collectors.toList());
        long wx_size = CollectionUtils.isEmpty(wx_msgs) ? 0 : wx_msgs.size();
        List<SendMsg4InitEntity> batchSaveLogs = Lists.newArrayList();
        boolean isFree = true;
        String summaryId = null;
        SMSChannel smsChannel = SMSChannel.TradeChannel;
        if (CollectionUtils.isNotEmpty(sms_msgs)) {
            final SMSSettingEntity sms_setting = getBean(SMSSettingEntityAction.class).loadByStoreId(store.getCompanyId(),
                    store.getId());
            final SMSProviderEntity smsProvider = getBean(SMSProviderEntityAction.class).loadSMSSupplier();
            SMSSendRuleEntity sendRule = getBean(SMSSendRuleEntityAction.class).loadByType(businessType);
            isFree = sendRule.isFreeSend();
            smsChannel = sendRule.isMarketChannel() ? SMSChannel.MarketChannel : SMSChannel.TradeChannel;
            if (sendRule.isMarketChannel()) {
                String sms_suffix = smsProvider.getSmsSuffix(SMSChannel.MarketChannel).orElse(null);
                sms_msgs.forEach(sms -> sms.addPrefixAndSuffix(sms_setting.getSmsPrefix(), sms_suffix));
            } else if (sendRule.isTradeChannel()) {
                sms_msgs.forEach(sms -> sms.addPrefix(sms_setting.getSmsPrefix()));
            } // end_if
            long sms_count = sms_msgs.stream().map(SMSEntity::getSmsNum).count();
            summaryId = getSummaryAction().insert(store, sendRule, null, sms_batch_no, sms_count, wx_size,
                    user.isAnonymous(), smsContext);
            if (!sendRule.isFreeSend()) {
                RechargeBalanceList balancesList = getBean(RechargeBalanceEntityAction.class).loadOrderEnabledByStore(store);
                List<ChargeDetailEntity> chargeDetails = balancesList.deduction(store, sms_batch_no, sms_count);
                List<RechargeBalanceEntity> rechargeBalances = balancesList.getDeductionList();
                getBean(ChargeDetailEntityAction.class).batchInsert(chargeDetails);
                getBean(RechargeBalanceEntityAction.class).batchUpdateBalance(rechargeBalances);
            }
        } else {
            summaryId = getSummaryAction().insert(store, null, businessType, sms_batch_no, 0L, wx_size,
                    user.isAnonymous(), smsContext);
        }

        for (SMSEntity sms : smses) {
            batchSaveLogs.add(SendMsg4InitEntity.createInstance(store, sms, sms_batch_no, smsChannel,
                    isFree, businessType));
        }
        // save_msg_log_4_sending
        getBean(SendMsg4InitEntityAction.class).batchInsert(batchSaveLogs);
        return summaryId;
    }

    void reimburseAction(Collection<SendMsg4ReimburseEntity> reimburses) {
        Map<String, Long> total = Maps.newConcurrentMap();
        reimburses.forEach(x -> {
            long size = MapUtils.getIntValue(total, x.getSendBatchNo(), 0) + x.getSmsCount();
            total.put(x.getSendBatchNo(), size);
        });

        if (logger.isDebugEnabled())
            logger.debug(String.format("writeOffService() total : %s", total));
        Multimap<String, ChargeDetailEntity> chargeDetail_map = getBean(ChargeDetailEntityAction.class).loadBySmsBatchNos(total.keySet());
        List<String> balanceIds = chargeDetail_map.entries().stream().map(x -> x.getValue().getBalanceId()).collect(Collectors.toList());
        List<RechargeBalanceEntity> balances = getBean(RechargeBalanceEntityAction.class).loadByIds(balanceIds);
        final List<ChargeDetailEntity> change_detail = Lists.newArrayList();
        final List<RechargeBalanceEntity> change_balance = Lists.newArrayList();
        final List<RechargeDetailEntity> change_recharge = Lists.newArrayList();
        total.forEach((k, v) -> {
            long size = v;
            Optional<RechargeBalanceEntity> balance;
            Collection<ChargeDetailEntity> details = chargeDetail_map.get(k);
            for (ChargeDetailEntity $it : details) {
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
            getBean(ChargeDetailEntityAction.class).batchWriteOff(change_detail);
            getBean(RechargeBalanceEntityAction.class).batchUpdateBalance(change_balance);
            getBean(RechargeDetailEntityAction.class).batchWriteOff(change_recharge);
            commitTx(ts);
        } catch (Exception e) {
            logger.error(String.format("[Event-Driver]事件发布失败.....%s", "writeOff"), e);
            rollbackTx(ts);
            throw e;
        }
    }

    private void recharge(RechargeReqDto rechargeDto) throws Exception {
        TransactionStatus ts = startTx(null);
        try {
            if (rechargeDto.isFreeCharge()) {
                getBean(SMSRechargeService.class).freecharge(rechargeDto.getCompanyId(), rechargeDto.getStoreGroupId(),
                        rechargeDto.getStoreId(), rechargeDto.getTotalQuantity());
            } else {
                if (rechargeDto.hasUnitPrice()) {
                    if (rechargeDto.isStoreRange()) {
                        getBean(SMSRechargeService.class).rechargeByStoreOnce(rechargeDto.getCompanyId(),
                                rechargeDto.getStoreId(),
                                rechargeDto.getRechargeAmount(), rechargeDto.getUnitPrice(), rechargeDto.getRemarke(),
                                rechargeDto.getRechargeType());
                    } else if (rechargeDto.isStoreGroupRange()) {
                        getBean(SMSRechargeService.class).rechargeByStoreGroupOnce(rechargeDto.getCompanyId(),
                                rechargeDto.getStoreGroupId(),
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
                                rechargeDto.getStoreGroupId(),
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
     * channel = BundleService.CHANNEL_SMS_SENDED
     *
     * @param user    浪人
     * @param payload 黑客
     */
    @SuppressWarnings("unchecked")
    public void handleSendResult(@Header(name = "user") LoginContext user,
                                 @Header(name = "action", required = false) String action,
                                 @Header(name = "sendBatchNo", required = false) String sendBatchNo,
                                 @Payload Object payload) {
        LoginContextHolder.setCtx(user);
        try {
            if (payload == null) return;
            if (payload instanceof SendMsg4SendEntity) {
                getBean(SendMsg4SendEntityAction.class).updateSendResulset((SendMsg4SendEntity) payload);
            } else {
                getBean(SendMsg4SendEntityAction.class).batchUpdate((List<SendMsg4SendEntity>) payload);
            }
        } finally {
            LoginContextHolder.clear();
        }
        if (StringUtils.equals("writeOff", action)) {
            Message<String> message = MessageBuilder.withPayload(sendBatchNo).setHeader("user", user)
                    .setHeader("action", "writeOff").build();
            getMessagingTemplate().sendAndReceive(BundleService.CHANNEL_SMS_BILLING, message);
        }
    }

    /**
     * 发送队列
     *
     * @param user  我是太阳
     * @param smses 待发送的短信
     */
    public void sendingSMSService(@Header(name = "user") LoginContext user, @Payload List<SendMsg4SendEntity> smses) {
        LoginContextHolder.setCtx(user);
        try {
            List<String> sms_set = smses.stream().map(sendLog -> SMSSendTransportProtocol.encoding4Flat(sendLog.getSms().getSmsId(),
                    sendLog.getCompanyId(), sendLog.getStoreId(), sendLog.getSmsChannel(),
                    sendLog.getSendStatus(), sendLog.getSms().getPhoneNo(), sendLog.getSms().getWordCount(),
                    sendLog.getSms().getSmsNum(), sendLog.getSms().getContent())).collect(Collectors.toList());
            Map<String, Object> params = Maps.newHashMap();
            params.put("payload", StringUtils.join(sms_set, "||"));
            Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                    .uri(getSendSmsApi(), "flat")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .syncBody(params)
                    .retrieve().bodyToMono(String.class);
            String result_rsp = mono.block(Duration.ofSeconds(30));
            if (logger.isDebugEnabled())
                logger.debug(String.format("sendingSMS(%s) result is %s", smses.size(), result_rsp));
            Optional<JsonElement> jsonElement = WebUtils.parseJson(result_rsp);
            // 257509ed-ac68-4961-9c29-6c6501f8a06d|9999|短信内容解码异常||100098|9999|短信内容解码异常
            Preconditions.checkState(jsonElement.isPresent(), "短信网关无返回数据");
            String rsp_payload = jsonElement.get().getAsString();
            String[] result_set = StringUtils.splitByWholeSeparator(rsp_payload, "||");
            Stream.of(result_set).forEach(result -> {
                try {
                    String[] _temp = StringUtils.split(result, '|');
                    Optional<SendMsg4SendEntity> sms_opt = smses.stream()
                            .filter(x -> StringUtils.equals(_temp[0], x.getSms().getSmsId())).findFirst();
                    sms_opt.ifPresent(sms -> {
                        if (StringUtils.equals("0000", _temp[1])) {
                            sms.finshedSend();
                        } else {
                            sms.errorBySending(_temp[2]);
                        }
                    });
                } catch (Exception e) {
                    logger.error("短信发送返回解析异常....", e);
                }
            });

        } catch (Exception e) {
            logger.error("发送短信到发生异常....", e);
        } finally {
            smses.forEach(x -> {
                if (x.isSending()) x.errorBySending(null);
            });
            Message<List<SendMsg4SendEntity>> msg_request = MessageBuilder.withPayload(smses)
                    .setHeader("user", LoginContextHolder.get()).build();
            getMessagingTemplate().send(BundleService.CHANNEL_SMS_SENDED, msg_request);
            LoginContextHolder.clear();
        }
    }

}
