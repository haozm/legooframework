package com.legooframework.model.security.entity;

import com.google.common.base.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountEntity extends BaseEntity<Long> {

    private String accountNo;
    private String accountName;
    private String password;
    private boolean enbaled;
    private boolean locked;
    private boolean canceled;
    private Set<String> authorities;

    AccountEntity(long id, String accountNo, String accountName, String password, Collection<RoleEntity> roles,
                  LoginContext loginContext) {
        super(id, loginContext.getTenantId(), loginContext.getLoginId());
        setAccountNo(accountNo);
        setPassword(password);
        this.accountName = accountName;
        this.enbaled = true;
        this.locked = false;
        this.canceled = false;
        if (CollectionUtils.isNotEmpty(roles)) {
            this.authorities = roles.stream().map(RoleEntity::getId).collect(Collectors.toSet());
        } else {
            this.authorities = null;
        }
    }

    AccountEntity(long id, ResultSet res) {
        super(id, res);
        try {
            setAccountNo(ResultSetUtil.getString(res, "accountNo"));
            setPassword(ResultSetUtil.getString(res, "password"));
            this.enbaled = ResultSetUtil.getBooleanByInt(res, "enbaled");
            this.accountName = ResultSetUtil.getOptString(res, "accountName", null);
            this.locked = ResultSetUtil.getBooleanByInt(res, "locked");
            this.canceled = ResultSetUtil.getBooleanByInt(res, "canceled");
            setRoleNos(ResultSetUtil.getStrSet(res, "authorities").orElse(null));
        } catch (SQLException e) {
            throw new RuntimeException("Restore AccountEntity has SQLException", e);
        }
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> datas = super.toParamMap("roleNos");
        datas.put("authorities", hasRoles() ? Joiner.on(',').join(this.authorities) : null);
        return datas;
    }

    private void setRoleNos(Set<String> roleNos) {
        this.authorities = CollectionUtils.isEmpty(roleNos) ? null : Sets.newHashSet(roleNos);
    }

    public Optional<Set<String>> getAuthorities() {
        return CollectionUtils.isEmpty(authorities) ? Optional.empty() : Optional.of(ImmutableSet.copyOf(authorities));
    }

    public boolean hasRoles() {
        return CollectionUtils.isNotEmpty(this.authorities);
    }

    public Optional<AccountEntity> bindingRoles(Collection<RoleEntity> roles, LoginContext lt) {
        Preconditions.checkArgument(!CollectionUtils.isEmpty(roles), "待绑定的角色列表不可以为空.");
        Preconditions.checkNotNull(lt);
        Preconditions.checkState(isEnbaled() || !isLocked(), "当前账户状态无效，无法绑定角色.");
        roles.forEach(x -> Preconditions.checkArgument(this.isSameTenant(x),
                "角色%s与账户不属于同一租户", x));
        Set<String> unbuild_roles = roles.stream().map(RoleEntity::getRoleNo).collect(Collectors.toSet());
        if (hasRoles()) {
            Sets.SetView<String> difference = Sets.difference(this.authorities, unbuild_roles);
            if (CollectionUtils.isEmpty(difference)) return Optional.empty();
            unbuild_roles.addAll(this.authorities);
            AccountEntity clone = (AccountEntity) cloneMe();
            clone.authorities = unbuild_roles;
            return Optional.of(clone);
        }
        AccountEntity clone = (AccountEntity) cloneMe();
        clone.authorities = unbuild_roles;
        return Optional.of(clone);
    }

    private void setAccountNo(String accountNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "账户不可以为空.");
        this.accountNo = accountNo;
    }

    public Optional<String> getAccountName() {
        return Optional.ofNullable(accountName);
    }

    public AccountEntity changeAccountName(String accountName) {
        Preconditions.checkState(isEnbaled(), "账户状态无效，无法修改密码.");
        Preconditions.checkState(!isLocked(), "账户状态锁定，无法修改密码.");
        Preconditions.checkState(!isCanceled(), "账户状态注销，无法修改密码.");
        if (StringUtils.equals(this.accountName, accountName)) return this;
        AccountEntity clone = (AccountEntity) cloneMe();
        clone.accountName = Strings.emptyToNull(accountName);
        return clone;
    }

    public String getShowName() {
        return Strings.isNullOrEmpty(this.accountName) ? this.accountNo : this.accountName;
    }

    private void setPassword(String password) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "密码不可以为空.");
        this.password = password;
    }

    public AccountEntity changePwd(String oldPwd, String newPwd) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(oldPwd));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newPwd));
        Preconditions.checkState(isEnbaled(), "账户状态无效，无法修改密码.");
        Preconditions.checkState(!isLocked(), "账户状态锁定，无法修改密码.");
        Preconditions.checkState(!isCanceled(), "账户状态注销，无法修改密码.");
        Preconditions.checkState(StringUtils.equals(this.getPassword(), oldPwd), "原密码不匹配，无法修改密码");
        if (StringUtils.equals(this.getPassword(), newPwd)) return this;
        AccountEntity clone = (AccountEntity) cloneMe();
        clone.setPassword(newPwd);
        return clone;
    }

    public AccountEntity enable(boolean enbale) {
        if (enbale == isEnbaled()) return this;
        Preconditions.checkState(!isCanceled(), "账户被注销，无法修改");
        Preconditions.checkState(!isLocked(), "账户被锁定，无法修改");
        AccountEntity clone = (AccountEntity) cloneMe();
        clone.enbaled = enbale;
        return clone;
    }

    public AccountEntity locked(boolean locked) {
        if (locked == isLocked()) return this;
        Preconditions.checkState(isEnbaled(), "账户无效，无法修改");
        Preconditions.checkState(!isCanceled(), "账户被注销，无法修改");
        AccountEntity clone = (AccountEntity) cloneMe();
        clone.locked = locked;
        return clone;
    }

    public AccountEntity canceled() {
        if (isCanceled()) return this;
        AccountEntity clone = (AccountEntity) cloneMe();
        clone.canceled = true;
        return clone;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnbaled() {
        return enbaled;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountEntity that = (AccountEntity) o;
        return enbaled == that.enbaled &&
                locked == that.locked &&
                canceled == that.canceled &&
                Objects.equal(accountNo, that.accountNo) &&
                Objects.equal(accountName, that.accountName) &&
                Objects.equal(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), accountNo, accountName, password, enbaled, locked, canceled);
    }

    @Override
    protected boolean equalsEntity(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountEntity that = (AccountEntity) o;
        return enbaled == that.enbaled &&
                locked == that.locked &&
                canceled == that.canceled &&
                Objects.equal(accountNo, that.accountNo) &&
                Objects.equal(getId(), that.getId()) &&
                Objects.equal(accountName, that.accountName) &&
                Objects.equal(password, that.password);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountNo", accountNo)
                .add("accountName", accountName)
                .add("password", "*******")
                .add("enbaled", enbaled)
                .add("locked", locked)
                .add("canceled", canceled)
                .add("authorities", authorities)
                .toString();
    }
}
