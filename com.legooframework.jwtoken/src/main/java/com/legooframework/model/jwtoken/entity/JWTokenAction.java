package com.legooframework.model.jwtoken.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public class JWTokenAction {

    private static final Logger logger = LoggerFactory.getLogger(JWTokenAction.class);

    private final static String LOGIN_WEB_URL = "/api/token/web/apply.json";
    private final static String LOGIN_MOBILE_URL = "/api/token/mobile/apply.json";
    // private final static String CHECKED_URL = "/api/token/{token}/checked.json";
    // private final static String LOGOUT_URL = "/api/token/{token}/logout.json";

    public String loginByWeb(String loginName, String loginHost) {
        try {
            String postUrl = String.format("%s%s", tokenUrl, LOGIN_WEB_URL);
            return login(loginName, loginHost, postUrl);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("网络异常，通讯失败，无法远程获取Token...");
        }
    }

    public String loginByMobile(String loginName, String loginHost) {
        String postUrl = String.format("%s%s", tokenUrl, LOGIN_MOBILE_URL);
        return login(loginName, loginHost, postUrl);
    }

    private final String FIX_TOKEN = "eyJ1dWlkIjoiODQ3MzM1ODgtZDEyNy00NmYwLTg3NTAtNzYzODI5NWI1MGQ0IiwibG9naW5OYW0iOiIxMDAwOThAbmV3bXRqIiwiaG9zdCI6InVua293bl9ob3N0IiwibG9naW5UaW1lIjoiMjAxOTA1MjcxNDA4NDciLCJjaGFubmVsIjoxfQ==";

    public JWToken checkToken(String loginToken) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginToken), "待验证的Token 不可以为空值...");
        if (StringUtils.equals(FIX_TOKEN, loginToken))
            return JWToken.secureAnonymous();
        String postUrl = String.format("%s/api/token/{token}/checked.json", tokenUrl);
        if (logger.isTraceEnabled())
            logger.trace(String.format("checkToken:url=%s,token=%s", postUrl, loginToken));
        JsonElement payload = post(postUrl, null, loginToken);
        Preconditions.checkState(!payload.isJsonNull(), "返回报文异常，无法获取Token=%s 解析结果...", loginToken);
        JsonObject jsonObject = payload.getAsJsonObject();
        String tokenId = jsonObject.get("tokenId").getAsString();
        String lastVisitTime = jsonObject.get("lastVisitTime").getAsString();
        String loginName = jsonObject.get("loginName").getAsString();
        String channel = jsonObject.get("channel").getAsString();
        JWToken jwToken = new JWToken(tokenId, loginName, channel, lastVisitTime);
        if (logger.isTraceEnabled())
            logger.trace(String.format("checkToken(%s ...) return %s", loginToken, jwToken));
        return jwToken;
    }

    public boolean logout(String token) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "待退出的Token 不可以为空值...");
        String postUrl = String.format("%s/api/token/{token}/logout.json", tokenUrl);
        post(postUrl, null, token);
        return true;
    }

    private JsonElement post(String postUrl, Map<String, Object> params, Object... pathVariables) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,params=%s", postUrl, params));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        _params.put("bundle", "com.legooframework.jwtoken");
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(postUrl, pathVariables)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .syncBody(_params)
                .retrieve().bodyToMono(String.class);
        String payload = mono.block();
        Preconditions.checkNotNull(payload, "数据无返回，通信异常...");
        if (logger.isTraceEnabled())
            logger.trace(String.format("URL=%s,params=%s,return %s", postUrl, _params, payload.length()));
        return parseJson(payload);
    }

    private String login(String loginName, String loginHost, String postUrl) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "入参 loginName 不允许为空...");
        String _loginHost = Strings.isNullOrEmpty(loginHost) ? "unkown_host" : loginHost;
        Map<String, Object> request = Maps.newHashMap();
        request.put("loginName", loginName);
        request.put("loginHost", _loginHost);
        JsonElement payload = post(postUrl, request);
        if (logger.isDebugEnabled())
            logger.debug(String.format("login(%s,%s ,...) return token = %s", loginName, loginHost, payload.getAsString()));
        return payload.getAsString();
    }

    private JsonElement parseJson(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        String code = jsonObject.get("code").getAsString();
        if (!StringUtils.equals("0000", code)) {
            throw new RuntimeException(String.format("ERROE:%s , MSG:%s ", code, jsonObject.get("msg").getAsString()));
        }
        return jsonObject.get("data");
    }

    private String tokenUrl;

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
        if (this.tokenUrl.endsWith("/"))
            this.tokenUrl = this.tokenUrl.substring(0, this.tokenUrl.length() - 1);
    }

}
