package com.legooframework.model.jwtservice.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Map;

public class JWToken implements Serializable {

    private static final int CHANNLE_WEB = 1;
    private static final int CHANNLE_MOBILE = 2;

    private final String uuid;
    private final String loginNam, host;
    private final String loginTime;
    private final int channel;

    private JWToken(String uuid, String loginNam, String host, int channle, LocalDateTime loginTime) {
        this.uuid = uuid;
        this.host = host;
        this.channel = channle;
        this.loginNam = loginNam;
        this.loginTime = loginTime.toString("yyyyMMddHHmmss");
    }

    static JWToken webToken(String uuid, String loginNam, String host, LocalDateTime loginTime) {
        return new JWToken(uuid, loginNam, host, CHANNLE_WEB, loginTime);
    }

    static JWToken mobileToken(String uuid, String loginNam, String host, LocalDateTime loginTime) {
        return new JWToken(uuid, loginNam, host, CHANNLE_MOBILE, loginTime);
    }

    String getHost() {
        return host;
    }

    int getChannel() {
        return channel;
    }

    String getUuid() {
        return uuid;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public String getLoginNam() {
        return loginNam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JWToken)) return false;
        JWToken token = (JWToken) o;
        return Objects.equal(uuid, token.uuid) &&
                Objects.equal(loginNam, token.loginNam) &&
                Objects.equal(host, token.host) &&
                this.channel == token.channel &&
                Objects.equal(loginTime, token.loginTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid, loginNam, host, loginTime, channel);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("loginName", loginNam);
        params.put("channel", channel == 1 ? "web" : "mobile");
        params.put("tokenId", uuid);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("loginNam", loginNam)
                .add("host", host)
                .add("channel", channel)
                .add("loginTime", loginTime)
                .toString();
    }
}
