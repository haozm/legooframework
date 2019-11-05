package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.smsgateway.entity.SMSBlackListEntity;
import com.legooframework.model.smsgateway.entity.SMSBlackListEntityAction;
import com.legooframework.model.smsgateway.entity.SendMsg4FinalEntity;
import com.legooframework.model.smsgateway.entity.SendMsg4FinalEntityAction;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SMSSyncDataService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SMSSyncDataService.class);

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
                    .uri(getSmsBlackApi())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve().bodyToMono(String.class);
            String http_return = mono.block(Duration.ofSeconds(30));
            if (logger.isDebugEnabled())
                logger.debug(String.format("URL=%s,params=%s,return %s", getSmsBlackApi(), params, http_return));
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

    /**
     * 同步最终短信发送结果
     */
    public void syncFinalStates() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            Optional<List<SendMsg4FinalEntity>> final_list = getBean(SendMsg4FinalEntityAction.class).load4FinalState(3000);
            final_list.ifPresent(ins -> {
                List<String> smsIds = ins.stream().map(BaseEntity::getId).collect(Collectors.toList());
                List<List<String>> list_list = Lists.partition(smsIds, 300);
                list_list.forEach(lt -> CompletableFuture.supplyAsync(() -> run(lt)).thenAccept(res -> LoginContextHolder.clear()));
            });
        } catch (Exception e) {
            logger.error("syncFinalStates has error", e);
        } finally {
            LoginContextHolder.clear();
        }
    }

    private String run(List<String> smsIds) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsIds", StringUtils.join(smsIds, ','));
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(getFinalStateApi())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve().bodyToMono(String.class);
        String http_return = mono.block(Duration.ofSeconds(30));
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,params=%s,return %s", getFinalStateApi(), params, http_return));
        Optional<JsonElement> payload_opt = WebUtils.parseJson(http_return);
        if (!payload_opt.isPresent()) return "OK";
        String payload_data = payload_opt.get().getAsString();
        String[] args = StringUtils.splitByWholeSeparator(payload_data, "|||");
        List<SendMsg4FinalEntity> instance_list = Lists.newArrayListWithCapacity(args.length);
        for (String str : args) {
            // 28372e35-ed81-4867-97fd-c94855b693b8|4|2|2019-05-22 18:33:00|error:NOTEXITS
            String[] arg = StringUtils.split(str, '|');
            instance_list.add(SendMsg4FinalEntity.getInstance(arg[0], Integer.valueOf(arg[1]),
                    Integer.valueOf(arg[2]), arg[3], arg[4]));
        }
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            getBean(SendMsg4FinalEntityAction.class).batchUpdate(instance_list);
        } finally {
            LoginContextHolder.clear();
        }
        return "OK";
    }

}
