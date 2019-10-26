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

public class SMSSendAndReceiveService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SMSSendAndReceiveService.class);
    private final Semaphore semaphore = new Semaphore(30);

    public void sendToSmsGateWay(@Payload Map<String, Object> payload) {
//        {id=3a1743ea-fa32-484a-b24f-a574cda69819, companyId=100098,
//        smsExt=6376813803, smsChannle=2, phoneNo=13826050862, smsContext=【梦特娇尚新】妈妈喊你回家吃饭 退订回T}
        final Semaphore _lock = semaphore;
        LoginContextHolder.setAnonymousCtx();
        try {
            _lock.acquire();
            SMSProviderEntity supplier = getSmsProviderAction().loadSMSSupplier();
            SMSChannel smsChannel = SMSChannel.paras(MapUtils.getIntValue(payload, "smsChannle"));
            String sendApi = supplier.getHttpSendUrl(smsChannel);
            String replay = sendSMSToPlatform(sendApi, MapUtils.getString(payload, "phoneNo"),
                    WebUtils.encodeUrl(MapUtils.getString(payload, "smsContext")),
                    MapUtils.getLong(payload, "smsExt"));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(replay), "短信网关响应报文为空...");
            if (StringUtils.startsWith(replay, "success:")) {
                finshSend(payload, replay.substring(8), replay);
            } else if (StringUtils.startsWith(replay, "error:")) {
                errorSend(payload, replay);
            } else {
                errorSend(payload, "GATEWATERROR");
            }
        } catch (Exception e) {
            logger.error(String.format("sendToSmsGateWay(%s) has error", payload), e);
            errorSend(payload, e.getMessage());
        } finally {
            LoginContextHolder.clear();
            _lock.release();
        }
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            getMessagingTemplate().send("sending_result_channel", MessageBuilder.withPayload(payload).build());
        } finally {
            LoginContextHolder.clear();
        }
    }

    private void finshSend(Map<String, Object> payload, String sendMsgId, String remarks) {
        payload.put("finalState", FinalState.SENDEDOK.getState());
        payload.put("sendMsgId", sendMsgId);
        payload.put("sendDate", DateTime.now().toDate());
        payload.put("remarks", remarks);
    }

    private void errorSend(Map<String, Object> payload, String remarks) {
        payload.put("finalState", FinalState.SENDEDERROR.getState());
        payload.put("sendMsgId", null);
        payload.put("sendDate", null);
        payload.put("remarks", remarks);
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
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .retrieve().bodyToMono(String.class);
        String send_rsp = mono.block(Duration.ofSeconds(30L));
        if (logger.isDebugEnabled())
            logger.debug(String.format("sendSMSToPlatform() and send_rsp=%s", send_rsp));
        return send_rsp;
    }

    // LIST CHANNEL_SYNC_STATE
    public void syncFromSmsGateWay(@Payload Map<String, Object> payload) {
        // {id=51300364-1a85-4764-905a-0000ea5f6dc0, companyId=100098, smsExt=4776124108, smsChannle=2, phoneNo=18575106652}
        LoginContextHolder.setAnonymousCtx();
        final Semaphore _lock = this.semaphore;
        try {
            _lock.acquire();
            SMSProviderEntity supplier = getSmsProviderAction().loadSMSSupplier();
            SMSChannel smsChannel = SMSChannel.paras(MapUtils.getIntValue(payload, "smsChannle"));
            String postUrl = supplier.getHttpStatusByMobilesUrl(smsChannel);
            Date sendDate = (Date) MapUtils.getObject(payload, "sendDate");
            Date startTime = LocalDateTime.fromDateFields(sendDate).plusMinutes(-10).toDate();
            Optional<List<Map<String, Object>>> rsp_payload = syncGateWayState(postUrl, MapUtils.getString(payload, "phoneNo"),
                    startTime, DateTime.now().toDate());
            rsp_payload.ifPresent(items -> items.forEach(item -> {
                getMessagingTemplate().send("result_result_channel", MessageBuilder.withPayload(item).build());
            }));
        } catch (Exception e) {
            logger.error(String.format("syncFromSmsGateWay(%s) has error", payload), e);
        } finally {
            _lock.release();
            LoginContextHolder.clear();
        }
    }

    private Optional<List<Map<String, Object>>> syncGateWayState(String postUrl, String mobile, Date start, Date end) {
        Map<String, Object> pathVariables = Maps.newHashMap();
        pathVariables.put("start", start.getTime() / 1000);
        pathVariables.put("end", end.getTime() / 1000);
        pathVariables.put("mobile", mobile);
        if (logger.isTraceEnabled())
            logger.trace(String.format("http://****/fehc&from=%s&to=%s&mobile=%s", MapUtils.getString(pathVariables, "start"),
                    MapUtils.getString(pathVariables, "end"), MapUtils.getString(pathVariables, "mobile")));
        Mono<String> mono = WebClient.create().method(HttpMethod.GET)
                .uri(postUrl, pathVariables)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .retrieve().bodyToMono(String.class);
        String records = mono.block(Duration.ofSeconds(30L));
        if (logger.isDebugEnabled())
            logger.debug("[FEHCHED-SMS-STATE]" + records);
        if (StringUtils.equals("no record", records) || StringUtils.startsWith(records, "error:"))
            return Optional.empty();
        //45732168869581,13824401814,UNDELIV,2019-05-23 14:24:55
        String[] payloads = StringUtils.split(records, ';');
        if (ArrayUtils.isEmpty(payloads)) return Optional.empty();
        List<Map<String, Object>> mapList = Lists.newArrayListWithCapacity(payloads.length);
        for (String payload : payloads) {
            String[] items = StringUtils.split(payload, ',');
            Map<String, Object> params = Maps.newHashMap();
            params.put("sendMsgId", items[0]);
            params.put("phoneNo", items[1]);
            params.put("finalStateDesc", items[2]);
            params.put("finalStateDate", DateTimeUtils.parseDateTime(items[3]).toDate());
            params.put("finalState", StringUtils.equals(items[2], "DELIVRD") ? FinalState.DELIVRD.getState() :
                    FinalState.UNDELIV.getState());
            mapList.add(params);
        }
        return Optional.of(mapList);
    }

}


