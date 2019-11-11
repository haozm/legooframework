package com.legooframework.model.smsresult.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsprovider.entity.SMSProviderEntity;
import com.legooframework.model.smsprovider.service.SendedSmsDto;
import com.legooframework.model.smsresult.entity.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

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
        payload.put("sendDate", DateTime.now().toDate());
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

    private String sendSMSToPlatform(String sendApi, String mobile, String content, Long smsExt) {
        Map<String, Object> pathVariables = Maps.newHashMap();
        pathVariables.put("mobile", mobile);
        pathVariables.put("ext", smsExt);
        pathVariables.put("content", content);
        if (logger.isTraceEnabled())
            logger.trace(String.format("http://******/ext=%s&mobile=%s&content=%s", smsExt, mobile, content));
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(sendApi, pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class);
        String send_rsp = mono.block(Duration.ofSeconds(30L));
        if (logger.isDebugEnabled())
            logger.debug(String.format("sendSMSToPlatform() and send_rsp=%s", send_rsp));
        return send_rsp;
    }

    // LIST CHANNEL_SYNC_STATE
    public void syncState(@Payload Map<String, Object> payload) {
        // {id=51300364-1a85-4764-905a-0000ea5f6dc0, companyId=100098, smsExt=4776124108, smsChannle=2, phoneNo=18575106652}
        LoginContextHolder.setAnonymousCtx();
        try {
            String account = MapUtils.getString(payload, "account");
            Date sendDate = (Date) MapUtils.getObject(payload, "sendDate");
            Date startTime = LocalDateTime.fromDateFields(sendDate).plusMinutes(-10).toDate();
            Optional<String> optional = getSmsService().sync(account, MapUtils.getString(payload, "phoneNo"),
                    startTime, DateTime.now().toDate());
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
        Optional<List<SMSReplayEntity>> list = getBean(SMSReplayEntityAction.class).load4TDEntities();
        List<SMSBlackListEntity> black_list = Lists.newArrayList();
        list.ifPresent(x -> x.forEach(y -> black_list.add(SMSBlackListEntity.creatInstance(y))));
        getBean(SMSBlackListEntityAction.class).batchInsert(black_list);
    }

    public void pullSmsGateWayReply() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        SMSProviderEntity provider = getSmsProviderAction().loadSMSSupplier();
        Optional<List<String>> postUrls = provider.getHttpReplayUrl();
        postUrls.ifPresent(urls -> urls.forEach(url -> {
            try {
                sendAndReceive(url);
            } catch (Exception e) {
                logger.error("pullSmsGateWayReplay has error ", e);
            } finally {
                LoginContextHolder.clear();
            }
        }));
    }

    private void sendAndReceive(String replayApi) {
        Map<String, Object> pathVariables = Maps.newHashMap();
        Mono<String> mono = WebClient.create().method(HttpMethod.GET)
                .uri(replayApi, pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class);
        String send_rsp = mono.block(Duration.ofSeconds(30L));
        if (logger.isDebugEnabled())
            logger.debug(String.format("replay_rsp=%s", send_rsp));
        if (StringUtils.equals("no record", send_rsp) || StringUtils.startsWith(send_rsp, "error:")) return;
        getBean(SMSReplayEntityAction.class).batchInsert(send_rsp);
    }
}


