package com.legooframework.model.smsresult.service;

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
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class SmsResultService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsResultService.class);

    private final Semaphore semaphore = new Semaphore(24);

    public void sending(@Payload Map<String, Object> payload) {
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
        getMessagingTemplate().send("channel_sms_result", MessageBuilder.withPayload(payload).build());
    }

    private void finshSend(Map<String, Object> payload, SendedSmsDto replayDto) {
        payload.put("sendState", SendState.SENDED.getState());
        payload.put("sendMsgId", replayDto.getSmsSendId());
        payload.put("sendDate", LocalDateTime.now().toDate());
        payload.put("sendRemark", replayDto.getExitsRespons());
        payload.put("account", replayDto.getAccount());
    }

    private void errorSend(Map<String, Object> payload, String account, String errMsg) {
        payload.put("sendState", SendState.ERROR.getState());
        payload.put("sendMsgId", null);
        payload.put("sendDate", LocalDateTime.now().toDate());
        payload.put("sendRemark", errMsg);
        payload.put("account", account);
    }

    private Map<String, Object> decodeState(String replay) {
        String[] items = StringUtils.split(replay, ',');
        Map<String, Object> params = Maps.newHashMap();
        if (items.length == 4) {
            params.put("sendMsgId", items[0]);
            params.put("phoneNo", items[1]);
            params.put("finalDesc", items[2]);
            params.put("finalDate", DateTimeUtils.parseDateTime(items[3]).toDate());
            params.put("finalState", StringUtils.equals(items[2], "DELIVRD") ? FinalState.DELIVRD.getState() :
                    FinalState.UNDELIV.getState());
        } else if (items.length == 5) {
            params.put("smsExt", items[0]);
            params.put("sendMsgId", items[1]);
            params.put("phoneNo", items[2]);
            params.put("finalDesc", items[3]);
            params.put("finalDate", DateTimeUtils.parseDateTime(items[4]).toDate());
            params.put("finalState", StringUtils.equals(items[3], "DELIVRD") ? FinalState.DELIVRD.getState() :
                    FinalState.UNDELIV.getState());
        } else {
            throw new IllegalStateException(String.format("异常返回状态报文：%s", replay));
        }
        return params;
    }

    public void batchSyncStateJob() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<SMSSubAccountEntity>> subAccounts = getSmsService().findEnabledSubAccounts();
        if (!subAccounts.isPresent()) return;
        try {
            for (SMSSubAccountEntity subAccount : subAccounts.get()) {
                Optional<String> optional = getSmsService().batchSync(subAccount.getUsername());
                if (!optional.isPresent()) continue;
                String[] payloads = StringUtils.split(optional.get(), ';');
                if (ArrayUtils.isEmpty(payloads)) continue;
                List<Map<String, Object>> mapList = Lists.newArrayListWithCapacity(payloads.length);
                Stream.of(payloads).forEach($it -> mapList.add(decodeState($it)));
                getBean(SMSResultEntityAction.class).updateState(mapList);
            }
        } catch (Exception e) {
            logger.error("batchSyncState() has error", e);
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


