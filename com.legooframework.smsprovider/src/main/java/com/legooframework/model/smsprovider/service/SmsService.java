package com.legooframework.model.smsprovider.service;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsprovider.entity.SMSProviderEntityAction;
import com.legooframework.model.smsprovider.entity.SMSSubAccountEntity;
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

public class SmsService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private SMSProviderEntityAction providerEntityAction;

    public void setProviderEntityAction(SMSProviderEntityAction providerEntityAction) {
        this.providerEntityAction = providerEntityAction;
    }

    public Optional<List<SMSSubAccountEntity>> findEnabledSubAccounts() {
        return providerEntityAction.findEnabledSubAccounts();
    }

    /**
     * 基于通道 发送 短信
     *
     * @param channel 发送渠道
     * @param mobile  手机号码
     * @param content 发送内容
     * @param smsExt  发送扩代码
     * @return 发送结果 以及发送账号
     */
    public SendedSmsDto send(SMSChannel channel, String mobile, String content, long smsExt) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("send(SMSChannel=%d, mobile=%s, smsExt=%d) start...",
                    channel.getChannel(), mobile, smsExt));
        SMSSubAccountEntity account = providerEntityAction.loadSubAccountByChannel(channel);
        Map<String, Object> pathVariables = Maps.newHashMap();
        pathVariables.put("mobile", mobile);
        pathVariables.put("ext", smsExt);
        pathVariables.put("content", content);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(account.getHttpSendUrl(), pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class);
        String response = mono.block(Duration.ofSeconds(40L));
        stopwatch.stop(); // optional
        if (logger.isDebugEnabled())
            logger.debug(String.format("send(mobile=%s, smsExt=%d) return %s [%s]", mobile, smsExt, response, stopwatch));
        return new SendedSmsDto(account, response);
    }

    /**
     * @param account 短信账户信息
     * @return response
     */
    public Optional<SyncSmsDto> reply(SMSSubAccountEntity account) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("reply(account=%s,url=%s) start...", account.getUsername(), account.getHttpReplyUrl()));
        Map<String, Object> pathVariables = Maps.newHashMap();
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(account.getHttpReplyUrl(), pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class);
        Stopwatch stopwatch = Stopwatch.createStarted();
        String response = mono.block(Duration.ofSeconds(60));
        stopwatch.stop(); // optional
        if (logger.isDebugEnabled())
            logger.debug(String.format("reply(account=%s,url=%s) return %s [%s]", account.getUsername(),
                    account.getHttpReplyUrl(), response, stopwatch));
        return Optional.ofNullable(Strings.isNullOrEmpty(response) ? null : new SyncSmsDto(account, response));
    }

    /**
     * 查询指定时间段内的 短信号码发送情况
     *
     * @param account 账户信息
     * @param mobile  手机号码
     * @param start   开始时间
     * @param end     结束时间
     * @return response
     */
    public Optional<String> sync(String account, String mobile, long start, long end) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("sync(account=%s, mobile=%s, start=%d, end=%d) start...",
                    account, mobile, start, end));
        SMSSubAccountEntity subAccount = providerEntityAction.loadSubAccountByAccount(account);
        Map<String, Object> pathVariables = Maps.newHashMap();
        pathVariables.put("start", start);
        pathVariables.put("end", end);
        pathVariables.put("mobile", mobile);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Mono<String> mono = WebClient.create().method(HttpMethod.GET)
                .uri(subAccount.getHttpStatusUrl(), pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class);
        String response = mono.block(Duration.ofSeconds(20L));
        stopwatch.stop(); // optional
        if (logger.isDebugEnabled())
            logger.debug(String.format("sync(account=%s, mobile=%s, start=%s, end=%s) return %s [%s]",
                    account, mobile, start, end, response, stopwatch));
        if (StringUtils.equals("no record", response) || StringUtils.startsWith(response, "error:"))
            return Optional.empty();
        return Optional.ofNullable(response);
    }

    /**
     * 查询指定时间段内的 短信号码发送情况
     *
     * @param account 账户信息
     * @return response
     */
    public Optional<String> batchSync(String account) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSync(account=%s) start...", account));
        SMSSubAccountEntity subAccount = providerEntityAction.loadSubAccountByAccount(account);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Mono<String> mono = WebClient.create().method(HttpMethod.GET)
                .uri(subAccount.getHttpBatchStatusUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class);
        String response = mono.block(Duration.ofSeconds(20L));
        stopwatch.stop(); // optional
        if (logger.isDebugEnabled())
            logger.debug(String.format("sync(account=%s) return %s [%s]", account, response, stopwatch));
        if (StringUtils.equals("no record", response) || StringUtils.startsWith(response, "error:"))
            return Optional.empty();
        return Optional.ofNullable(response);
    }
}
