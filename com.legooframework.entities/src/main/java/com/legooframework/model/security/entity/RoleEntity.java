package com.legooframework.model.security.entity;

import com.google.common.base.*;
import com.google.common.collect.ImmutableSet;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.Sorting;
import com.legooframework.model.core.base.runtime.LegooRole;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.security.core.GrantedAuthority;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleEntity extends BaseEntity<String> implements GrantedAuthority, Sorting {

    private String roleName, roleDesc, authority;
    private boolean enbaled;
    private Set<String> resources;
    private int priority; // 角色优先级,越大越好

    RoleEntity(String roleNo, String roleName, String roleDesc, int priority, LoginContext loginContext) {
        super(roleNo, loginContext.getTenantId(), loginContext.getLoginId());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(roleNo));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(roleName));
        this.authority = String.format("ROLE_%s", roleNo);
        this.roleName = roleName;
        this.enbaled = true;
        this.priority = priority;
        this.roleDesc = roleDesc;
    }

    RoleEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.authority = String.format("ROLE_%s", id);
            this.enbaled = ResultSetUtil.getBooleanByInt(res, "enbaled");
            this.roleName = res.getString("roleName");
            this.roleDesc = res.getString("roleDesc");
            this.priority = res.getInt("priority");
            this.resources = ResultSetUtil.getStrSet(res, "resources").orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore RoleEntity has SQLException", e);
        }
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> data = super.toParamMap("resources");
        data.put("resources", CollectionUtils.isEmpty(this.resources) ? null : Joiner.on(',').join(this.resources));
        return data;
    }

    public LegooRole toLegooRole() {
        return new LegooRole(this.getId(), this.roleName, this.priority);
    }


    public RoleEntity enable(boolean enbale) {
        if (enbale == isEnbaled()) return this;
        RoleEntity clone = (RoleEntity) cloneMe();
        clone.enbaled = enbale;
        return clone;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public Optional<Set<String>> getResourceIds() {
        return Optional.ofNullable(CollectionUtils.isEmpty(resources) ? null :
                ImmutableSet.copyOf(resources));
    }

    public Optional<RoleEntity> bindingResources(Collection<ResEntity> resources) {
        Preconditions.checkArgument(!CollectionUtils.isEmpty(resources), "待绑定的资源ID不可以为空...");
        Preconditions.checkState(isEnbaled(), "当前角色状态无效，无法绑定资源.");
        Set<String> unbuilds = resources.stream().map(BaseEntity::getId).collect(Collectors.toSet());
        if (exitsResources()) {
            if (SetUtils.isEqualSet(unbuilds, this.resources)) return Optional.empty();
            RoleEntity clone = (RoleEntity) cloneMe();
            unbuilds.addAll(this.resources);
            clone.resources = unbuilds;
            return Optional.of(clone);
        }
        RoleEntity clone = (RoleEntity) cloneMe();
        clone.resources = unbuilds;
        return Optional.of(clone);
    }

    @Override
    protected boolean equalsEntity(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalsEntity(o)) return false;
        RoleEntity that = (RoleEntity) o;
        return Objects.equal(this.getId(), that.getId()) &&
                Objects.equal(roleName, that.roleName);
    }

    public boolean exitsResources() {
        return CollectionUtils.isNotEmpty(resources);
    }

    public Set<String> getResources() {
        return ImmutableSet.copyOf(resources);
    }

    Optional<RoleEntity> clearResource() {
        if (exitsResources()) {
            RoleEntity clone = (RoleEntity) cloneMe();
            clone.resources = null;
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    public String getRoleNo() {
        return getId();
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public boolean isEnbaled() {
        return enbaled;
    }

    @Override
    public int getIndex() {
        return priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoleEntity that = (RoleEntity) o;
        return enbaled == that.enbaled &&
                Objects.equal(getId(), that.getId()) &&
                Objects.equal(roleName, that.roleName) &&
                Objects.equal(roleDesc, that.roleDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), roleName, roleDesc, enbaled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("tenantId", getTenantId())
                .add("authority", authority)
                .add("enbaled", enbaled)
                .add("roleName", roleName)
                .add("roleDesc", roleDesc)
                .add("resourceIds' size", CollectionUtils.isEmpty(resources) ? 0 : resources.size())
                .toString();
    }
}
