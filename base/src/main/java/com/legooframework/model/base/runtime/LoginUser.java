package com.legooframework.model.base.runtime;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.utils.CommonsUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LoginUser implements LoginContext {

    private Long loginId, tenantId, accountId;
    private String userName, password, accountNo, deviceNo;
    private Set<GrantedAuthority> authorities;
    private List<Long> storeIds;
    private List<LegooOrg> stores;
    private Set<String> roleNos, storeDevices;
    private final DateTime loginTime;
    private List<LegooRole> roles;

    protected LoginUser(Long loginId, Long accountId, String accountNo, String deviceNo, String password, Long tenantId,
                        Collection<LegooOrg> stores, List<LegooRole> legooRoles, Collection<String> storeDevices) {
        this.accountId = accountId;
        this.loginId = loginId;
        this.userName = String.format("%s@%s", accountNo, deviceNo);
        this.accountNo = accountNo;
        this.deviceNo = deviceNo;
        this.password = password;
        this.tenantId = tenantId;
        this.loginTime = DateTime.now();
        this.authorities = Sets.newHashSet();
        this.roleNos = Sets.newHashSet();
        this.storeDevices = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
        setStores(stores);
        if (CollectionUtils.isNotEmpty(storeDevices)) this.storeDevices.addAll(storeDevices);
        if (CollectionUtils.isNotEmpty(legooRoles)) {
            this.roles = CommonsUtils.getOrdering().sortedCopy(legooRoles);
            roles.forEach(x -> {
                this.roleNos.add(x.getRoleNo());
                this.authorities.add(new SimpleGrantedAuthority(String.format("ROLE_%s", x)));
            });
        }
    }

    void setStores(Collection<LegooOrg> stores) {
        if (CollectionUtils.isNotEmpty(stores)) {
            this.stores = ImmutableList.copyOf(stores);
            List<Long> storeIds = this.stores.stream().map(LegooOrg::getId).collect(Collectors.toList());
            this.storeIds = ImmutableList.copyOf(storeIds);
        } else {
            this.stores = null;
            this.storeIds = null;
        }
    }

    @Override
    public Optional<Collection<String>> getDeviceId() {
        return Optional.ofNullable(CollectionUtils.isNotEmpty(storeDevices) ? storeDevices : null);
    }

    @Override
    public Optional<String> getToken() {
        return Optional.empty();
    }

    protected LoginUser(Long loginId, String accountNo, Long tenantId) {
        this.loginId = loginId == null ? -1L : loginId;
        this.accountNo = Strings.isEmpty(accountNo) ? "Anonymou" : accountNo;
        this.deviceNo = "null";
        this.userName = String.format("%s@%s", this.accountNo, this.deviceNo);
        this.accountId = -1L;
        this.password = "{noop}123456";
        this.tenantId = tenantId == null ? -1L : tenantId;
        this.storeIds = null;
        this.loginTime = DateTime.now();
        this.stores = null;
        this.authorities = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOU"));
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
    }

    @Override
    public String getAccountNo() {
        return this.accountNo;
    }

    @Override
    public String getDeviceNo() {
        return deviceNo;
    }

    @Override
    public Date getLoginTime() {
        return this.loginTime.toDate();
    }

    @Override
    public Optional<List<LegooOrg>> getStores() {
        return Optional.ofNullable(CollectionUtils.isEmpty(stores) ? null : this.stores);
    }

    @Override
    public Long getAccountId() {
        return this.accountId;
    }

    @Override
    public Optional<List<Long>> getStoreIds() {
        return Optional.ofNullable(CollectionUtils.isEmpty(this.storeIds) ? null : this.storeIds);
    }

    @Override
    public Optional<Long> getOnlyOneStoreId() {
        if (CollectionUtils.isEmpty(this.storeIds)) return Optional.empty();
        Preconditions.checkState(this.storeIds.size() == 1, "当前用户绑定多家门店，非法调用该方法...");
        return Optional.of(this.storeIds.get(0));
    }

    @Override
    public Long getLoginId() {
        return loginId;
    }

    @Override
    public String getUsername() {
        return userName;
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
    public boolean equalsUser(LoginContext o) {
        if (this == o) return true;
        if (!(o instanceof LoginUser)) return false;
        LoginUser loginUser = (LoginUser) o;
        return Objects.equal(loginId, loginUser.loginId) &&
                Objects.equal(userName, loginUser.userName) &&
                Objects.equal(tenantId, loginUser.tenantId);
    }

    protected Set<String> getRoleNos() {
        return roleNos;
    }

    @Override
    public Optional<List<LegooRole>> getRoles() {
        return Optional.ofNullable(CollectionUtils.isEmpty(roles) ? null : roles);
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
        params.put("USER_ID", getAccountId());
        params.put("HAS_STORE", CollectionUtils.isNotEmpty(this.stores));
        params.put("STORE_IDS", CollectionUtils.isNotEmpty(this.stores) ? this.storeIds : new Long[]{-1L});
        params.put("ACCOUNT_NO", getAccountNo());
        params.put("DEVICE_NO", getDeviceNo());
        params.put("TENANT_ID", getTenantId());
        params.put("STORE_DEV_IDS", getDeviceId().orElse(null));
        params.put("ROLE_NOS", this.roleNos);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("loginId", loginId)
                .add("tenantId", tenantId)
                .add("accountId", accountId)
                .add("accountNo", accountNo)
                .add("deviceNo", deviceNo)
                .add("userName", userName)
                .add("password", password)
                .add("authorities", authorities)
                .add("stores", this.stores)
                .toString();
    }
}
