package com.legooframework.model.jwtservice.entity;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class JWTokenBuilder {

    private static final Logger logger = LoggerFactory.getLogger(JWTokenBuilder.class);
    private final String uuid;
    private String loginName, host;
    private int channle;
    private LocalDateTime loginTime;

    private JWTokenBuilder(String uuid) {
        this.uuid = uuid;
    }

    static JWTokenBuilder getBuilder() {
        return new JWTokenBuilder(UUID.randomUUID().toString());
    }

    JWTokenBuilder setLoginName(String loginName) {
        this.loginName = loginName;
        return this;
    }

    JWTokenBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    JWTokenBuilder setWebChannle() {
        this.channle = 1;
        return this;
    }

    JWTokenBuilder setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
        return this;
    }

    public String encodeBase64() {
        JWToken token;
        if (this.channle == 1) {
            token = JWToken.webToken(uuid, loginName, Strings.isEmpty(host) ? "unknow_host" : host,
                    loginTime == null ? LocalDateTime.now() : loginTime);
        } else {
            token = JWToken.mobileToken(uuid, loginName, Strings.isEmpty(host) ? "unknow_host" : host,
                    loginTime == null ? LocalDateTime.now() : loginTime);
        }
        String gson = WebUtils.toJson(token);
        return Base64.encodeBase64String(gson.getBytes(Charsets.UTF_8));
    }

    static String encodeBase64(JWToken token) {
        String gson = WebUtils.toJson(token);
        return Base64.encodeBase64String(gson.getBytes(Charsets.UTF_8));
    }


    static JWToken decodeBase64(String token_str) {
//        {"uuid":"ca1d5aa5-2fe0-40f2-a98d-f23755510771","cid":1,"uid":12,
//                "sid":333,"cname":"asd","uname":"234234","sname":"asdasd",
//                "roles":[{"roleId":3,"roleName":"dianzhag"},{"roleId":6,"roleName":"datopi"}],
//            "orgIds":[2,4,5,56],"loginTime":"20190128221123"}
        byte[] bts = Base64.decodeBase64(token_str);
        String gson = new String(bts, Charsets.UTF_8);
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(gson);
        String uuid = jsonObject.get("uuid").getAsString();
        String loginName = jsonObject.get("loginName").getAsString();
        int channel = jsonObject.get("channel").getAsInt();
        String host = jsonObject.get("host").getAsString();
        String loginTime = jsonObject.get("loginTime").getAsString();
        JWToken token;
        if (channel == 1) {
            token = JWToken.webToken(uuid, loginName, host, DateTimeUtils.parseYYYYMMDDHHMMSS(loginTime));
        } else {
            token = JWToken.mobileToken(uuid, loginName, host, DateTimeUtils.parseYYYYMMDDHHMMSS(loginTime));
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("decodeBase64(%s ...) -> %s", token_str.substring(0, 12), token));
        return null;
    }

}
