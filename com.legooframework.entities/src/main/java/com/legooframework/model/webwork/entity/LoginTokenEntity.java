package com.legooframework.model.webwork.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class LoginTokenEntity extends BaseEntity<String> {

    private Long accountId;
    private int status;
    private String accountNo, deviceId, loginHost, fullToken;
    private DateTime loginTime, logoutTime, lastTime;

    LoginTokenEntity(WebUserDetails loginUser) {
        super(loginUser.getShortToken(), loginUser.getTenantId(), loginUser.getAccountId());
        this.accountId = loginUser.getAccountId();
        this.status = 1;
        this.accountNo = loginUser.getAccountNo();
        this.deviceId = loginUser.getDeviceNo();
        this.loginHost = "127.0.0.1";
        this.loginTime = new DateTime(loginTime);
        this.lastTime = this.loginTime;
        this.logoutTime = null;
        Map<String, Object> data_json = Maps.newHashMap();
        data_json.put("accountNo", loginUser.getAccountNo());
        data_json.put("deviceNo", loginUser.getDeviceNo());
        data_json.put("loginId", loginUser.getLoginId());
        data_json.put("accountId", loginUser.getAccountId());
        data_json.put("tenantId", loginUser.getTenantId());
        data_json.put("userLabel", loginUser.getUserLabel());
        data_json.put("storeDevices", loginUser.getDeviceId().orElse(null));
        data_json.put("loginTime", DateFormatUtils.format(loginUser.getLoginTime(), "yyyy-MM-dd HH:mm:ss"));
        this.fullToken = WebUtils.toJson(data_json);
    }

    LoginTokenEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.accountId = ResultSetUtil.getObject(res, "accountId", Long.class);
            this.accountNo = ResultSetUtil.getObject(res, "accountNo", String.class);
            this.deviceId = ResultSetUtil.getObject(res, "deviceId", String.class);
            this.loginHost = ResultSetUtil.getObject(res, "loginHost", String.class);
            this.fullToken = ResultSetUtil.getObject(res, "fullToken", String.class);
            this.status = res.getInt("status");
            this.lastTime = new DateTime(res.getDate("lastTime"));
            this.loginTime = new DateTime(res.getDate("loginTime"));
            this.logoutTime = res.getObject("logoutTime") == null ? null : new DateTime(res.getDate("logoutTime"));
        } catch (SQLException e) {
            throw new RuntimeException("Restore LoginTokenEntity has SQLException", e);
        }
    }

    void setLoginTime(DateTime loginTime) {
        this.loginTime = loginTime;
    }

    public boolean isLogin() {
        return 1 == status;
    }

    public boolean isLogout() {
        return 0 == status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLoginHost() {
        return loginHost;
    }

    public DateTime getLoginTime() {
        return loginTime;
    }

    public DateTime getLogoutTime() {
        return logoutTime;
    }

    public String getFullToken() {
        return fullToken;
    }

    public DateTime getLastTime() {
        return lastTime;
    }

    public void remarksLastTime() {
        this.lastTime = DateTime.now();
    }

    public boolean equalsSameUser(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginTokenEntity that = (LoginTokenEntity) o;
        return Objects.equal(accountId, that.accountId) &&
                Objects.equal(accountNo, that.accountNo) &&
                Objects.equal(deviceId, that.deviceId) &&
                Objects.equal(fullToken, that.fullToken);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LoginTokenEntity that = (LoginTokenEntity) o;
        return status == that.status &&
                Objects.equal(accountId, that.accountId) &&
                Objects.equal(accountNo, that.accountNo) &&
                Objects.equal(deviceId, that.deviceId) &&
                Objects.equal(loginHost, that.loginHost) &&
                Objects.equal(fullToken, that.fullToken) &&
                Objects.equal(loginTime, that.loginTime) &&
                Objects.equal(logoutTime, that.logoutTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), accountId, status, accountNo, deviceId, loginHost, fullToken, loginTime, logoutTime);
    }

    public Optional<LoginTokenEntity> logout() {
        if (isLogout()) return Optional.empty();
        this.loginTime = DateTime.now();
        this.status = 0;
        return Optional.of(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("accountId", accountId)
                .add("status", status)
                .add("accountNo", accountNo)
                .add("deviceId", deviceId)
                .add("loginHost", loginHost)
                .add("loginTime", loginTime)
                .add("logoutTime", logoutTime)
                .add("fullToken", fullToken)
                .toString();
    }
}
