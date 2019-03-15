package com.legooframework.model.core.base.runtime;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoginUser implements LoginContext {

    private Long loginId, tenantId;
    private String userName, password, loginName, token;
    private Set<GrantedAuthority> authorities;
    private final DateTime loginTime;
    private Integer storeId, orgId;
    private List<Integer> storeIds;

    protected LoginUser(Long loginId, String userName, String password, Long companyId, Set<String> roles) {
        this.loginId = loginId;
        this.userName = userName;
        this.password = password;
        this.tenantId = companyId;
        this.loginTime = DateTime.now();
        this.authorities = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
        if (CollectionUtils.isNotEmpty(roles)) {
            roles.forEach(x -> this.authorities.add(new SimpleGrantedAuthority(x)));
        }
    }

    public LoginUser(Long loginId, Long tenantId, String userName, String password,
                     Collection<String> roles, Integer storeId, Integer orgId, List<Integer> storeIds) {
        this.loginId = loginId;
        this.tenantId = tenantId;
        this.userName = userName;
        this.password = password;
        this.authorities = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
        if (CollectionUtils.isNotEmpty(roles)) {
            roles.forEach(x -> this.authorities.add(new SimpleGrantedAuthority(x)));
        }
        this.loginTime = DateTime.now();
        this.storeId = storeId;
        this.orgId = orgId;
        if (this.tenantId.intValue() == this.orgId)
            this.orgId = -1;
        if (CollectionUtils.isNotEmpty(storeIds)) this.storeIds = storeIds;
    }

    LoginUser(Long loginId, String accountNo, Long tenantId) {
        this.loginId = loginId == null ? -1L : loginId;
        this.password = "{noop}123456";
        this.tenantId = tenantId == null ? -1L : tenantId;
        this.loginTime = DateTime.now();
        this.authorities = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOU"));
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Integer getStoreId() {
        return storeId;
    }

    public String getToken() {
        return token;
    }

    @Override
    public Long getLoginId() {
        return loginId;
    }

    @Override
    public String getUsername() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        if (Strings.isNullOrEmpty(this.loginName))
            this.loginName = loginName;
    }

    @Override
    public boolean isStoreManager() {
        return this.authorities.stream().allMatch(x -> x.getAuthority().equals("ROLE_StoreManagerRole"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean exitsRoles(String... roleNos) {
        return true;
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginUser)) return false;
        LoginUser loginUser = (LoginUser) o;
        return Objects.equal(loginId, loginUser.loginId) &&
                Objects.equal(userName, loginUser.userName) &&
                Objects.equal(tenantId, loginUser.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(loginId, userName, tenantId);
    }

    @Override
    public Map<String, Object> toParams() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("LOGIN_ID", getLoginId());
        params.put("TENANT_ID", getTenantId());
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("loginId", loginId)
                .add("tenantId", tenantId)
                .add("userName", userName)
                .add("password", password)
                .add("authorities", authorities)
                .toString();
    }
}
