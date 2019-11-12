package com.legooframework.model.smsresult.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsprovider.entity.SMSSubAccountEntity;
import com.legooframework.model.smsprovider.service.SendedSmsDto;
import com.legooframework.model.smsresult.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class SmsResultService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsResultService.class);
    private final Semaphore semaphore = new Semaphore(24);

    public void sending(@Payload Map<String, Object> payload) {
//        {id=3a1743ea-fa32-484a-b24f-a574cda69819, companyId=100098,
//        smsExt=6376813803, smsChannle=2, phoneNo=13826050862, smsContext=【梦特娇尚新】妈妈喊你回家吃饭 退订回T}
        final Semaphore _lock = semaphore;
        LoginContextHolder.setAnonymousCtx();
        try {
            _lock.acquire();
            SMSChannel smsChannel = SMSChannel.paras(MapUtils.getIntValue(payload, "smsChannle"));
            SendedSmsDto replayDto = getSmsService().send(smsChannel, MapUtils.getString(payload, "phoneNo"),
                    WebUtils.encodeUrl(MapUtils.getString(payload, "smsContext")),
                    MapUtils.getLong(payload, "smsExt"));
            if (replayDto.isSuccess()) {
                this.finshSend(payload, replayDto);
            } else if (replayDto.isError()) {
                this.errorSend(payload, replayDto.getAccount(), replayDto.getResponse().orElse(null));
            } else {
                this.errorSend(payload, replayDto.getAccount(), replayDto.getResponse().orElse(null));
            }
        } catch (Exception e) {
            logger.error(String.format("sendToSmsGateWay(%s) has error", payload), e);
            this.errorSend(payload, null, e.getMessage());
        } finally {
            LoginContextHolder.clear();
            _lock.release();
        }
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            getMessagingTemplate().send("channel_sms_result", MessageBuilder.withPayload(payload).build());
        } finally {
            LoginContextHolder.clear();
        }
    }

    private void finshSend(Map<String, Object> payload, SendedSmsDto replayDto) {
        payload.put("finalState", FinalState.SENDEDOK.getState());
        payload.put("sendMsgId", replayDto.getSmsSendId());
        payload.put("sendDate", LocalDateTime.now().toDate());
        payload.put("remarks", replayDto.getExitsRespons());
        payload.put("account", replayDto.getAccount());
    }

    private void errorSend(Map<String, Object> payload, String account, String errMsg) {
        payload.put("finalState", FinalState.SENDEDERROR.getState());
        payload.put("sendMsgId", null);
        payload.put("sendDate", null);
        payload.put("remarks", errMsg);
        payload.put("account", account);
    }

    /**
     * @param start hour
     * @param end   hour
     */
    public void manualSyncState(int start, int end) {
        Optional<List<Map<String, Object>>> payload = getBean(SMSResultEntityAction.class).load4SyncState(start, end);
        if (!payload.isPresent()) return;
        getMessagingTemplate().send("channel_sync_source", MessageBuilder.withPayload(payload.get()).build());
        if (logger.isDebugEnabled())
            logger.debug(String.format("manualSyncState(...) is sending to queue...,size is %d", payload.get().size()));
    }

    // LIST CHANNEL_SYNC_STATE
    public void syncState(@Payload Map<String, Object> payload) {
        // {id=51300364-1a85-4764-905a-0000ea5f6dc0, companyId=100098, smsExt=4776124108, smsChannle=2, phoneNo=18575106652}
        LoginContextHolder.setAnonymousCtx();
        try {
            String account = MapUtils.getString(payload, "account");
            Date sendDate = (Date) MapUtils.getObject(payload, "sendDate");
            long start = LocalDateTime.fromDateFields(sendDate).plusMinutes(-10).toDate().getTime() / 1000;
            Optional<String> optional = getSmsService().sync(account, MapUtils.getString(payload, "phoneNo"),
                    start, LocalDateTime.now().toDate().getTime() / 1000);
            if (!optional.isPresent()) return;
            String[] payloads = StringUtils.split(optional.get(), ';');
            if (ArrayUtils.isEmpty(payloads)) return;
            List<Map<String, Object>> mapList = Lists.newArrayListWithCapacity(payloads.length);
            for (String $it : payloads) {
                String[] items = StringUtils.split($it, ',');
                Map<String, Object> params = Maps.newHashMap();
                params.put("sendMsgId", items[0]);
                params.put("phoneNo", items[1]);
                params.put("finalStateDesc", items[2]);
                params.put("finalStateDate", DateTimeUtils.parseDateTime(items[3]).toDate());
                params.put("finalState", StringUtils.equals(items[2], "DELIVRD") ? FinalState.DELIVRD.getState() :
                        FinalState.UNDELIV.getState());
                mapList.add(params);
            }
            getBean(SMSResultEntityAction.class).updateState(mapList);
        } catch (Exception e) {
            logger.error(String.format("syncState(%s) has error", payload), e);
        } finally {
            LoginContextHolder.clear();
        }
    }

    public void refresh4BlackList() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<SMSReplyEntity>> list = getBean(SMSReplyEntityAction.class).load4TDEntities();
        List<SMSBlackListEntity> black_list = Lists.newArrayList();
        list.ifPresent(x -> x.forEach(y -> black_list.add(SMSBlackListEntity.creatInstance(y))));
        getBean(SMSBlackListEntityAction.class).batchInsert(black_list);
    }

    /**
     * 接受用户的返回短信息
     */
    public void replay() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<List<SMSSubAccountEntity>> subAccounts = getSmsService().findEnabledSubAccounts();
        if (!subAccounts.isPresent()) return;
        List<CompletableFuture<Void>> cfs = Lists.newArrayListWithCapacity(subAccounts.get().size());
        for (SMSSubAccountEntity $it : subAccounts.get()) {
            CompletableFuture.supplyAsync(() -> getSmsService().reply($it)).thenAccept(opt -> opt.ifPresent(dto -> {
                LoginContextHolder.setIfNotExitsAnonymousCtx();
                try {
                    getBean(SMSReplyEntityAction.class).batchInsert(dto);
                } finally {
                    LoginContextHolder.clear();
                }
            }));
        }
        if (CollectionUtils.isNotEmpty(cfs)) {
            CompletableFuture.allOf(cfs.toArray(new CompletableFuture[]{})).join();
        }
        if (logger.isDebugEnabled())
            logger.debug("replay() ..... completable....");
    }

}


