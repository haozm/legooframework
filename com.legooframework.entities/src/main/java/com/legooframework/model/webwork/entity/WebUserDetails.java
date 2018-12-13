package com.legooframework.model.webwork.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LegooOrg;
import com.legooframework.model.core.base.runtime.LegooRole;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.organization.entity.CompanyEntity;
import com.legooframework.model.organization.entity.EmployeeEntity;
import com.legooframework.model.security.entity.AccountEntity;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WebUserDetails extends LoginUser {

    private final DateTime loginTime;
    private final String userLabel;
    private final CompanyEntity company;
    private LoginTokenEntity loginToken;

    public WebUserDetails(AccountEntity account, List<LegooRole> legooRoles, EmployeeEntity employee,
                          Collection<LegooOrg> stores, CompanyEntity company, String deviceno,
                          LoginTokenEntity loginToken, Collection<String> storeDevices) {
        super(employee.getId(), account.getId(), account.getAccountNo(), deviceno,
                account.getPassword(), account.getTenantId(), stores, legooRoles, storeDevices);
        this.userLabel = employee.getUserName();
        this.loginTime = DateTime.now();
        this.company = company;
        this.loginToken = loginToken;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public Map<String, Object> getUserDetailView() {
        Map<String, Object> user = Maps.newHashMap();
        user.put("account", getAccountNo());
        user.put("loginId", getLoginId());
        user.put("accountId", getAccountId());
        user.put("userName", this.userLabel);
        user.put("tenantId", getTenantId());
        user.put("roles", getRoleNos());
        user.put("storeDevices", getDeviceId().map(x -> Joiner.on(',').join(x)));
        user.put("loginTime", loginTime.toString("yyyy-MM-dd HH:mm:ss"));
        return user;
    }

    public LoginTokenEntity getLoginToken() {
        if (loginToken != null) return loginToken;
        loginToken = new LoginTokenEntity(this);
        return loginToken;
    }

    @Override
    public Optional<String> getToken() {
        return Optional.ofNullable(loginToken == null ? null : loginToken.getId());
    }

    public String getShortToken() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("accountNo", getAccountNo());
        data.put("accountId", getAccountId());
        data.put("loginId", getLoginId());
        data.put("devideNo", getDeviceNo());
        data.put("tenantId", getTenantId());
        data.put("storeDevices", getDeviceId().map(x -> Joiner.on(',').join(x)));
        data.put("loginTime", DateFormatUtils.format(getLoginTime(), "yyyy-MM-dd HH:mm:ss"));
        return DigestUtils.md5Hex(WebUtils.toJson(data));
    }

    public String getUserLabel() {
        return userLabel;
    }
}
