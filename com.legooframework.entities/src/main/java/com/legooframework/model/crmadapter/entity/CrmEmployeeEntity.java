package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class CrmEmployeeEntity extends BaseEntity<Integer> {

    private final Integer loginId;
    // 1  启用  2 停用
    private Integer companyId, organizationId, storeId, employeeState;
    private String userName;
    private Collection<Integer> roleIds;

    CrmEmployeeEntity(Integer id, ResultSet res) {
        super(id, res);
        try {
            String role_ids = res.getString("roleIds");
            if (!Strings.isNullOrEmpty(role_ids)) {
                this.roleIds = Sets.newHashSet();
                Stream.of(StringUtils.split(role_ids, ',')).forEach(x -> roleIds.add(Integer.valueOf(x)));
            } else {
                this.roleIds = null;
            }
            this.organizationId = ResultSetUtil.getOptObject(res, "organizationId", Integer.class).orElse(null);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(null);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.loginId = ResultSetUtil.getObject(res, "loginId", Integer.class);
            this.employeeState = ResultSetUtil.getObject(res, "employeeState", Integer.class);
            this.userName = ResultSetUtil.getOptString(res, "userName", null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore CrmEmployeeEntity has SQLException", e);
        }
    }

    public boolean isDianzhang() {
        return getRoleIds().isPresent() && getRoleIds().get().contains(5);
    }

    public boolean isDaogou() {
        return getRoleIds().isPresent() && getRoleIds().get().contains(7);
    }

    public Optional<Collection<Integer>> getRoleIds() {
        return Optional.ofNullable(roleIds);
    }

    private void setEmployeeState(Integer employeeState) {
        this.employeeState = employeeState;
    }

    public Optional<Integer> getOrganizationId() {
        return Optional.ofNullable(organizationId);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isEnabled() {
        return 1 == this.employeeState;
    }

    public Optional<Integer> getStoreId() {
        return Optional.ofNullable(storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("loginId", loginId)
                .add("companyId", companyId)
                .add("organizationId", organizationId)
                .add("storeId", storeId)
                .add("employeeState", employeeState)
                .add("userName", userName)
                .add("roleIds", roleIds)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrmEmployeeEntity that = (CrmEmployeeEntity) o;
        return Objects.equals(loginId, that.loginId) &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(employeeState, that.employeeState) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(roleIds, that.roleIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), loginId, companyId, organizationId, storeId, employeeState, userName, roleIds);
    }
}
