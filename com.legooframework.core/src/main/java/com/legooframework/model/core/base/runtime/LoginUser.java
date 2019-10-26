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

import java.util.*;

public class LoginUser implements LoginContext {

    private Long loginId, tenantId;
    private String userName, password, loginName, token, companyName, storeName;
    private Set<GrantedAuthority> authorities;
    private Collection<Integer> roleIds;
    private final DateTime loginTime;
    private Integer storeId, orgId;
    private List<Integer> storeIds;

    @Override
    public Optional<Collection<Integer>> getStoreIds() {
        return Optional.ofNullable(CollectionUtils.isEmpty(storeIds) ? null : storeIds);
    }


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
                     Collection<String> roles, Collection<Integer> roleIds, Integer storeId, Integer orgId, List<Integer> storeIds,
                     String companyName, String storeName) {
        this.loginId = loginId;
        this.tenantId = tenantId;
        this.userName = userName;
        this.password = password;
        this.companyName = companyName;
        this.storeName = storeName;
        this.authorities = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
        if (CollectionUtils.isNotEmpty(roles)) {
            roles.forEach(x -> this.authorities.add(new SimpleGrantedAuthority(x)));
        }
        this.loginTime = DateTime.now();
        this.storeId = storeId;
        this.orgId = orgId;
        this.roleIds = roleIds;
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
    public String getLoginName() {
        return userName;
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
    public boolean isManager() {
        Optional<?> opt = this.authorities.stream().filter(x -> x.getAuthority().equals("ROLE_ManagerRole")).findFirst();
        return opt.isPresent();
    }

    @Override
    public boolean isShoppingGuide() {
        Optional<?> opt = this.authorities.stream().filter(x -> x.getAuthority().equals("ROLE_ShoppingGuideRole")).findFirst();
        return opt.isPresent();
    }

    @Override
    public boolean isStoreManager() {
        Optional<?> opt = this.authorities.stream().filter(x -> x.getAuthority().equals("ROLE_StoreManagerRole")).findFirst();
        return opt.isPresent();
    }

    @Override
    public boolean isRegediter() {
        Optional<?> opt = this.authorities.stream().filter(x -> x.getAuthority().equals("ROLE_AdminRole")).findFirst();
        return opt.isPresent();
    }

    @Override
    public boolean isAreaManagerRole() {
        Optional<?> opt = this.authorities.stream().filter(x -> x.getAuthority().equals("ROLE_AreaManagerRole")).findFirst();
        return opt.isPresent();
    }

    @Override
    public boolean isBoss() {
        Optional<?> opt = this.authorities.stream().filter(x -> x.getAuthority().equals("ROLE_BossRole")).findFirst();
        return opt.isPresent();
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
        params.put("STORE_IDS", CollectionUtils.isEmpty(storeIds) ? new Integer[0] : storeIds);
        params.put("STORE_ID", this.storeId == null ? -1 : storeId);
        params.put("ROLE_IDS", CollectionUtils.isEmpty(roleIds) ? new Integer[]{-1} : roleIds);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("loginId", loginId)
                .add("userName", userName)
                .add("companyId", tenantId)
                .add("storeId", storeId)
                .add("storeIds", storeIds)
                .add("authorities", authorities)
                .toString();
    }
}
