package com.legooframework.model.jwtoken.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.Serializable;

public class JWToken implements Serializable {

    private final String tokenId;
    private final String loginName;
    private final String channel;
    private final String lastVisitTime;

    JWToken(String tokenId, String loginName, String channle, String lastVisitTime) {
        this.tokenId = tokenId;
        this.channel = channle;
        this.loginName = loginName;
        this.lastVisitTime = lastVisitTime;
    }

    static JWToken secureAnonymous() {
        return new JWToken("SecureAnonymous", "Secure@Anonymous", "1", LocalDateTime.now().toString());
    }

    public boolean isAnonymous() {
        return StringUtils.equals("SecureAnonymous", this.tokenId) && StringUtils.equals("Secure@Anonymous", this.loginName);
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getChannel() {
        return channel;
    }

    public String getLastVisitTime() {
        return lastVisitTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JWToken)) return false;
        JWToken jwToken = (JWToken) o;
        return Objects.equal(tokenId, jwToken.tokenId) &&
                Objects.equal(loginName, jwToken.loginName) &&
                Objects.equal(channel, jwToken.channel) &&
                Objects.equal(lastVisitTime, jwToken.lastVisitTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tokenId, loginName, channel, lastVisitTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tokenId", tokenId)
                .add("loginName", loginName)
                .add("channel", channel)
                .add("lastVisitTime", lastVisitTime)
                .toString();
    }
}
