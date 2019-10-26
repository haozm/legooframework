package com.csosm.module.base.entity;

import com.csosm.commons.entity.OrderAbledUtil;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RoleSet {
    private final Integer userId;
    private final List<RoleEntity> roleSet;

    RoleSet(EmployeeEntity employee, List<RoleEntity> roleSet) {
        this.userId = employee.getId();
        this.roleSet = CollectionUtils.isEmpty(roleSet) ? null : ImmutableList.copyOf(roleSet);
    }

    RoleSet(Collection<RoleEntity> roleSet) {
        this.userId = -1;
        this.roleSet = CollectionUtils.isEmpty(roleSet) ? null : ImmutableList.copyOf(roleSet);
    }

    public boolean isOwner(EmployeeEntity employee) {
        Preconditions.checkArgument(null != employee);
        return Objects.equal(userId, employee.getId());
    }

    public boolean hasAnyRole() {
        return !CollectionUtils.isEmpty(roleSet);
    }

    public List<Object> getRoleIdList() {
        if (hasAnyRole()) {
            List<Object> ids = Lists.newArrayListWithCapacity(roleSet.size());
            for (RoleEntity cur : roleSet) {
                ids.add(cur.getId());
            }
            return ImmutableList.copyOf(ids);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    public Optional<RoleEntity> getMaxPowerRole() {
        if (CollectionUtils.isEmpty(roleSet)) return Optional.absent();
        List<RoleEntity> sorts = OrderAbledUtil.reverse(roleSet);
        return Optional.of(sorts.get(0));
    }

    public Collection<RoleEntity> getRoleSet() {
        if (hasAnyRole()) return roleSet;
        throw new RuntimeException("not exits any role ");
    }


    public boolean hasDBARole() {
        if (hasAnyRole()) {
            for (RoleEntity cur : roleSet) {
                if (cur.isDBA()) return true;
            }
        }
        return false;
    }

    public boolean hasAdminRole() {
        if (hasAnyRole()) {
            for (RoleEntity cur : roleSet) {
                if (cur.isAdmin()) return true;
            }
        }
        return false;
    }

    public boolean hasBossRole() {
        if (hasAnyRole()) {
            for (RoleEntity cur : roleSet) {
                if (cur.isBoss()) return true;
            }
        }
        return false;
    }

    public boolean hasManagerRole() {
        if (hasAnyRole()) {
            for (RoleEntity cur : roleSet) {
                if (cur.isManager()) return true;
            }
        }
        return false;
    }

    public boolean hasStoreManagerRole() {
        if (hasAnyRole()) {
            for (RoleEntity cur : roleSet) {
                if (cur.isStoreManager()) return true;
            }
        }
        return false;
    }

    public boolean hasShoppingGuideRole() {
        if (hasAnyRole()) {
            for (RoleEntity cur : roleSet) {
                if (cur.isShoppingGuide()) return true;
            }
        }
        return false;
    }

    public Set<String> getAllResources() {
        Set<String> set = Sets.newHashSet();
        if (CollectionUtils.isEmpty(roleSet)) return set;
        for (RoleEntity cur : roleSet) {
            cur.getResources().ifPresent(set::addAll);
        }
        return set;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("roleSet", roleSet)
                .toString();
    }
}
