package com.legooframework.model.jwtservice.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class JWTokenEntity extends BaseEntity<String> implements BatchSetter {

    private final String loginToken, loginName, loginHost;
    private final int channel;
    private String remark;
    private final LocalDateTime loginDateTime;
    private LocalDateTime logoutDateTime, lastVisitTime;
    private final int expiredTime;

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("loginToken", "loginName", "loginHost", "channel",
                "remark", "loginDateTime", "logoutDateTime", "lastVisitTime", "expiredTime");
        params.put("loginToken", loginToken);
        params.put("loginName", loginName);
        params.put("loginHost", loginHost);
        params.put("loginChannle", channel);
        params.put("remark", remark);
        params.put("loginDateTime", loginDateTime.toDate());
        params.put("lastVisitTime", lastVisitTime.toDate());
        params.put("expiredTime", expiredTime);
        params.put("logoutDateTime", logoutDateTime == null ? null : logoutDateTime.toDate());
        return params;
    }

    JWTokenEntity(String loginName, String host, int channel) {
        super(UUID.randomUUID().toString());
        this.loginDateTime = LocalDateTime.now();
        JWToken jwToken = channel == 1 ? JWToken.webToken(getId(), loginName, host, loginDateTime)
                : JWToken.mobileToken(getId(), loginName, host, loginDateTime);
        this.loginToken = JWTokenBuilder.encodeBase64(jwToken);
        this.loginName = loginName;
        this.channel = channel;
        this.loginHost = host;
        this.lastVisitTime = this.loginDateTime;
        this.logoutDateTime = null;
        if (channel == 1) {
            this.expiredTime = 2;
        } else {
            this.expiredTime = 12;
        }
    }

    JWToken getJwToken() {
        return channel == 1 ? JWToken.webToken(getId(), loginName, "127.0.0.1", loginDateTime)
                : JWToken.mobileToken(getId(), loginName, "127.0.0.1", loginDateTime);
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, LocalDateTime.now());
        ps.setObject(2, remark);
        ps.setObject(3, getId());
    }

    JWTokenEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.loginToken = ResultSetUtil.getString(res, "loginToken");
            this.loginHost = ResultSetUtil.getString(res, "loginHost");
            this.channel = ResultSetUtil.getObject(res, "loginChannel", Integer.class);
            this.loginName = ResultSetUtil.getString(res, "loginName");
            String _loginDateTime = DateFormatUtils.format(res.getDate("loginDateTime"), "yyyyMMddHHmmss");
            this.loginDateTime = DateTimeUtils.parseYYYYMMDDHHMMSS(_loginDateTime);
            Date _logoutDateTime = res.getDate("logoutDateTime");
            if (_logoutDateTime == null) {
                this.logoutDateTime = null;
            } else {
                this.logoutDateTime = DateTimeUtils.parseYYYYMMDDHHMMSS(DateFormatUtils
                        .format(_logoutDateTime, "yyyyMMddHHmmss"));
            }
            String _lastVisitTime = DateFormatUtils.format(res.getDate("lastVisitTime"), "yyyyMMddHHmmss");
            this.lastVisitTime = DateTimeUtils.parseYYYYMMDDHHMMSS(_lastVisitTime);
            this.expiredTime = ResultSetUtil.getObject(res, "expiredTime", Integer.class);
        } catch (SQLException e) {
            throw new RuntimeException("Restore JWTokenEntity has SQLException", e);
        }
    }

    String getLoginToken() {
        return loginToken;
    }

    void logout() {
        this.logoutDateTime = LocalDateTime.now();
    }

    String getRemark() {
        return remark;
    }

    public String getLoginHost() {
        return loginHost;
    }

    boolean isExpired() {
        return Hours.hoursBetween(LocalDateTime.now(), this.lastVisitTime).getHours() > expiredTime;
    }

    boolean isLogout() {
        return null != logoutDateTime;
    }

    void expired() {
        Preconditions.checkState(Hours.hoursBetween(LocalDateTime.now(), this.lastVisitTime).getHours() > expiredTime,
                "非发的操作,当前Token 尚未过期...");
        this.logoutDateTime = LocalDateTime.now();
        this.remark = "Token超时,系统退出...";
    }

    void touched() {
        Preconditions.checkState(!isLogout(), "Token 已经为登出状态...");
        Preconditions.checkState(Hours.hoursBetween(LocalDateTime.now(), this.lastVisitTime).getHours() < expiredTime,
                "非发的操作,当前Token 尚未过期...");
        this.lastVisitTime = LocalDateTime.now();
    }


    public LocalDateTime getLoginDateTime() {
        return loginDateTime;
    }

    public LocalDateTime getLogoutDateTime() {
        return logoutDateTime;
    }

    String getLoginName() {
        return loginName;
    }

    LocalDateTime getLastVisitTime() {
        return lastVisitTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JWTokenEntity)) return false;
        if (!super.equals(o)) return false;
        JWTokenEntity that = (JWTokenEntity) o;
        return channel == that.channel &&
                Objects.equal(loginToken, that.loginToken) &&
                Objects.equal(loginName, that.loginName) &&
                Objects.equal(loginHost, that.loginHost) &&
                Objects.equal(loginDateTime, that.loginDateTime) &&
                Objects.equal(logoutDateTime, that.logoutDateTime) &&
                Objects.equal(lastVisitTime, that.lastVisitTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), loginToken, loginName, channel, loginHost, loginDateTime, logoutDateTime, lastVisitTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("loginToken", loginToken)
                .add("loginName", loginName)
                .add("channel", channel)
                .add("loginHost", loginHost)
                .add("loginDateTime", loginDateTime)
                .add("logoutDateTime", logoutDateTime)
                .add("lastVisitTime", lastVisitTime)
                .toString();
    }
}
