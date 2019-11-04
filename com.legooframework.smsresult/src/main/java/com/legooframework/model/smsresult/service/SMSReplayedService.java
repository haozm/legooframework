package com.legooframework.model.smsresult.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.smsprovider.entity.SMSProviderEntity;
import com.legooframework.model.smsresult.entity.SMSBlackListEntity;
import com.legooframework.model.smsresult.entity.SMSBlackListEntityAction;
import com.legooframework.model.smsresult.entity.SMSReplayEntity;
import com.legooframework.model.smsresult.entity.SMSReplayEntityAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SMSReplayedService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SMSReplayedService.class);

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


