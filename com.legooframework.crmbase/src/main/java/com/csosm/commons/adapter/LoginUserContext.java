package com.csosm.commons.adapter;

import com.csosm.commons.entity.Replaceable;
import com.csosm.module.base.entity.*;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登陆用户上下文,实体聚合 用于登陆用户状态传递以及拼接SQL等场景
 *
 * @author Smart
 */
public class LoginUserContext implements Replaceable, UserDetails {

    private final EmployeeEntity employee;
    private final OrganizationEntity company;
    private final OrganizationEntity organization;
    private final StoreEntity store;
    // 角色信息
    private final RoleSet roleSet;
    // web登陆信息
    private String loginName = null;
    private String loginDomain = "http://localhost:8080/";
    private String ip, token;
    private final Set<GrantedAuthority> authorities;
    // 标记当前用户所有管辖的下级门店ID列表
    private final Set<Integer> subStoreIds;
    private final Set<Integer> subOrgs;
    private final boolean hasStoreView;

    public String getToken() {
        return token;
    }

    public void setLoginName(String loginName) {
        if (this.loginName == null)
            this.loginName = loginName;
    }

    public String getName() {
        return employee.getUserName();
    }

    public Object getUserId() {
        return this.employee.getId();
    }

    public String getCompanyName() {
        return this.company.getName();
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public Optional<Set<Integer>> getSubOrgs() {
        return Optional.fromNullable(subOrgs);
    }

    public Optional<OrganizationEntity> getCompany() {
        return Optional.fromNullable(company);
    }

    public Optional<RoleEntity> getMaxPowerRole() {
        return roleSet.getMaxPowerRole();
    }

    public Optional<OrganizationEntity> getOrganization() {
        return Optional.fromNullable(organization);
    }

    public Optional<StoreEntity> getStore() {
        return Optional.fromNullable(store);
    }

    public Optional<Set<Integer>> getSubStoreIds() {
        return Optional.fromNullable(CollectionUtils.isEmpty(subStoreIds) ? null : subStoreIds);
    }

    public StoreEntity getExitsStore() {
        Preconditions.checkState(this.store != null, "用户%s无门店信息...", this.getName());
        return this.store;
    }

    public Integer getOrgDeep() {
        return 0;
    }

    public void setToken(String token) {
        if (this.token == null)
            this.token = token;
    }

    public RoleSet getRoleSet() {
        return roleSet;
    }

    public LoginUserContext(StoreEntity store, OrganizationEntity company, String loginDomain, String ip) {
        this.employee = null;
        this.organization = null;
        this.company = company;
        this.roleSet = null;
        this.ip = Strings.isNullOrEmpty(ip) ? "localhost" : ip;
        this.loginDomain = loginDomain;
        this.store = store;
        this.subOrgs = null;
        this.token = "token";
        this.subStoreIds = null;
        this.authorities = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
        this.hasStoreView = false;
    }

    public LoginUserContext(EmployeeEntity employee, OrganizationEntity organization, Optional<StoreEntity> store,
                            OrganizationEntity company, RoleSet roleSet, String loginDomain,
                            String ip, List<Integer> subStoreIds, Set<Integer> subOrgs, String token, boolean hasStoreView) {
        this.employee = employee;
        this.organization = organization;
        this.company = company;
        this.roleSet = roleSet;
        if (null != roleSet)
            Preconditions.checkState(roleSet.isOwner(employee));
        this.ip = Strings.isNullOrEmpty(ip) ? "localhost" : ip;
        this.loginDomain = loginDomain;
        this.subOrgs = CollectionUtils.isEmpty(subOrgs) ? null : subOrgs;
        this.subStoreIds = CollectionUtils.isEmpty(subStoreIds) ? Sets.<Integer>newHashSet() : Sets.newHashSet(subStoreIds);
        // 当前登录用户为店长角色,必须指定所属的门店
        if (roleSet != null && !CollectionUtils.isEmpty(roleSet.getRoleSet())
                && roleSet.getMaxPowerRole().isPresent() && roleSet.hasStoreManagerRole()) {
            Preconditions.checkArgument(store.isPresent(), "当前用户为店长角色，需指定所管辖的门店");
        }
        this.authorities = Sets.newHashSet();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_LOGINER"));
        if (roleSet != null && !CollectionUtils.isEmpty(roleSet.getRoleSet())) {
            Collection<RoleEntity> roles = roleSet.getRoleSet();
            roles.forEach(x -> this.authorities.add(new SimpleGrantedAuthority(x.getAuthority())));
        }

        if (store.isPresent())
            this.subStoreIds.add(store.get().getId());
        this.token = token;
        this.store = store.orNull();
        this.hasStoreView = hasStoreView;

    }

    public Optional<String> getLoginDomain() {
        return Optional.fromNullable(loginDomain);
    }

    public String getIp() {
        return ip;
    }

    public boolean hasDianZhangRole() {
        return this.roleSet.hasStoreManagerRole();
    }

    public static LoginUserContext anonymous() {
        return new LoginUserContext(EmployeeEntity.anonymous(), null, Optional.<StoreEntity>absent(),
                null, null, null, "127.0.0.1", null, null, "token", false);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("姓名", employee == null ? "" : employee.getUserName())
                .add("所在组织", getOrganization().isPresent() ? organization.getName() : "未分配")
                .add("所在门店", getStore().isPresent() ? store.getName() : "未分配")
                .add("所在公司", null == company ? "未分配" : company.getName())
                .add("角色列表", roleSet == null ? "" : roleSet)
                .add("IP地址", ip)
                .add("登陆域名", loginDomain)
                .toString();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("USER_ID", employee.getId());
        map.put("LOGIN_ID", employee.getLoginUserId());
        map.put("MAX_ROLE", getMaxPowerRole().isPresent() ? getMaxPowerRole().get().getId() : -99);
        map.put("USER_NAME", Strings.emptyToNull(employee.getUserName()));
        map.put("USER_COMPANY_ID", company == null ? -9999 : company.getId());
        map.put("USER_COMPANY_NAME", company == null ? "公司" : company.getName());
        map.put("USER_STORE_ID", null == store ? null : store.getId());
        map.put("USER_STORE_NAME", null == store ? "无门店" : store.getName());
        map.put("USER_STORE_ID_QUERY", null == store ? -1 : store.getId());
        map.put("USER_ORG_ID", organization == null ? -9999 : organization.getId());
        map.put("USER_ORG_CODE", organization == null ? "-9999" : organization.getCode());
        map.put("USER_ORG_LEVEL", organization == null ? -1 : organization.getLevel());
        map.put("USER_ORG_NAME", organization == null ? "NO_ORG_NAME" : organization.getName());
        map.put("STORE_DEVICEID", store == null ? "NO_STORE_DEVICEID" : store.getContactTableName());
        map.put("STORE_RFM_SETTING", null == store ? null : store.getRfmSetting());
        map.put("COMPANY_RFM_SETTING", null == company ? null : company.getRfmSetting());
        map.put("USER_ORG_ST_NAME", company == null ? null : company.getShortName());
        map.put("MSG_COM_STORE", String.format("MSG_%s_%s", company == null ? 0 : company.getId(), store == null ? 0 : store.getId()));
        map.put("USER_IP", ip);
        map.put("USER_DOMAIN", loginDomain);
        map.put("SUB_STORES", CollectionUtils.isEmpty(this.subStoreIds) ? Lists.newArrayListWithCapacity(0) : this.subStoreIds);
        map.put("USER_ROLE_IDS", roleSet.hasAnyRole() ? roleSet.getRoleIdList() : Lists.newArrayListWithCapacity(0));
        map.put("HAS_STORE_VIEW", this.hasStoreView);
        List<Integer> all_stores = Lists.newArrayList();
        if (this.store != null) all_stores.add(store.getId());
        if (!CollectionUtils.isEmpty(this.subStoreIds)) all_stores.addAll(this.subStoreIds);
        map.put("ALL_STORES", all_stores);
        return map;
    }

    public boolean hasStoreView() {
        return this.hasStoreView;
    }

    @Override
    public Map<String, String> toSmsMap(StoreEntity store) {
        if (store == null) store = this.store;
        Map<String, String> map = Maps.newHashMap();
        map.put("{公司名称}", company == null ? "公司" : (company.getName() == null ? "" : company.getName()));
        map.put("{门店名称}", store == null ? "门店" : store.getName());
        map.put("{职员姓名}", employee == null ? "导购" : (employee.getUserName() == null ? "" : employee.getUserName()));
        return map;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return employee.getPassowrd();
    }

    @Override
    public String getUsername() {
        return loginName;
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
}
