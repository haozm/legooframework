package com.legooframework.model.core.base.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class HttpBaseEntityAction<T extends BaseEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HttpBaseEntityAction.class);

    /**
     * @param httpUrl       URL
     * @param params        params
     * @param timeout       超时
     * @param pathVariables path占位符
     * @return OOXX
     */
    protected Optional<String> post(String httpUrl, Map<String, Object> params, int timeout, Object... pathVariables) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(httpUrl), "非法的URL地址...%s", httpUrl);
        int _time = timeout <= 0 ? 60 * 5 : timeout;
        if (logger.isDebugEnabled())
            logger.debug(String.format("httpUrl=%s,params.size =%s,seconds=%d, pathVariables=%s", httpUrl,
                    MapUtils.isEmpty(params) ? 0 : params.size(), timeout, Arrays.toString(pathVariables)));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        _params.put("_batchId", UUID.randomUUID().toString());
        Stopwatch stopwatch = Stopwatch.createStarted();
        Mono<String> mono;
        if (ArrayUtils.isEmpty(pathVariables)) {
            mono = WebClient.create().method(HttpMethod.POST)
                    .uri(httpUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(_params)
                    .retrieve().bodyToMono(String.class);
        } else {
            mono = WebClient.create().method(HttpMethod.POST)
                    .uri(httpUrl, pathVariables)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(_params)
                    .retrieve().bodyToMono(String.class);
        }

        String response = mono.block(Duration.ofSeconds(_time));
        stopwatch.stop(); // optional
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,_batchId=%s,response=%s [%s]", httpUrl, MapUtils.getString(_params, "_batchId"),
                    response, stopwatch));
        return Optional.ofNullable(Strings.emptyToNull(response));
    }
}
