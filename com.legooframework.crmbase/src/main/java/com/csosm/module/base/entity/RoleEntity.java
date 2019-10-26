package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.OrderAbled;
import com.csosm.commons.entity.ResultSetUtil;
import com.google.common.base.*;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RoleEntity extends BaseEntity<Integer> implements OrderAbled, GrantedAuthority {

    private String roleName, roleDesc, authority;
    private boolean enbaled;
    private Set<String> resources;
    private Integer companyId;
    private int priority; // 角色优先级,越大越好

    int getPriority() {
        return priority;
    }

    RoleEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.roleName = res.getString("roleName");
            this.authority = String.format("ROLE_%s", this.roleName);
            this.enbaled = ResultSetUtil.getOptValue(res, "enbaled", Integer.class).or(0) == 1;
            this.roleDesc = res.getString("roleDesc");
            this.priority = res.getInt("priority");
            this.companyId = res.getInt("tenantId");
            String _res = res.getString("resources");
            if (Strings.isNullOrEmpty(_res)) {
                this.resources = null;
            } else {
                this.resources = Sets.newHashSet(StringUtils.split(_res, ','));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore RoleEntity has SQLException", e);
        }
    }

    boolean hasResources() {
        return CollectionUtils.isNotEmpty(resources);
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> params = super.toMap();
        params.put("resources", CollectionUtils.isEmpty(resources) ? null : Joiner.on(',').join(resources));
        params.put("companyId", companyId);
        return params;
    }

    Optional<RoleEntity> authorized(List<String> resourceIds) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(resourceIds), "待授权的资源列表不可以为空...");
        if (SetUtils.isEqualSet(this.resources, resourceIds)) return Optional.empty();
        try {
            RoleEntity clone = (RoleEntity) this.clone();
            clone.resources = Sets.newHashSet(resourceIds);
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    Optional<RoleEntity> clearResources() {
        if (CollectionUtils.isEmpty(this.resources)) return Optional.empty();
        try {
            RoleEntity clone = (RoleEntity) this.clone();
            clone.resources = null;
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Set<String>> getResources() {
        return Optional.ofNullable(CollectionUtils.isEmpty(resources) ? null : resources);
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public int getOrdering() {
        return priority;
    }

    public boolean isDBA() {
        return "ManagerRole".equals(roleName);
    }

    public boolean isAdmin() {
        return "AdminRole".equals(roleName);
    }

    public boolean isShoppingGuide() {
        return "ShoppingGuideRole".equals(roleName);
    }

    public boolean isBoss() {
        return "BossRole".equals(roleName);
    }

    public boolean isStoreManager() {
        return "StoreManagerRole".equals(roleName);
    }

    public boolean isManager() {
        return "AreaManagerRole".equals(roleName);
    }
    
    public boolean isRole(String roleName) {
    	return StringUtils.equals(this.roleName, roleName);
    }
    
    public boolean isRoleOf(Collection<String> roleNames) {
    	if(roleNames.contains("*")) return true;
    	return roleNames.contains(this.roleName);
    }
    
    public boolean isLeader() {
        return isAdmin() || isBoss() || isManager();
    }

    public boolean isEnabled() {
        return enbaled;
    }

    public String getDesc() {
        return roleDesc;
    }

    public String getName() {
        return roleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity)) return false;
        if (!super.equals(o)) return false;
        RoleEntity that = (RoleEntity) o;
        return enbaled == that.enbaled &&
                priority == that.priority &&
                Objects.equal(roleName, that.roleName) &&
                Objects.equal(roleDesc, that.roleDesc) &&
                Objects.equal(authority, that.authority) &&
                Objects.equal(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), roleName, roleDesc, authority, enbaled, resources, priority);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("roleId", getId())
                .add("roleName", roleName)
                .add("roleDesc", roleDesc)
                .add("authority", authority)
                .add("enbaled", enbaled)
                .add("resources", resources)
                .add("priority", priority)
                .toString();
    }

}
