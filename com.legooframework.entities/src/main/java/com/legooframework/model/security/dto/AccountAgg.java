package com.legooframework.model.security.dto;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.runtime.LegooRole;
import com.legooframework.model.security.entity.AccountEntity;
import com.legooframework.model.security.entity.RoleEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountAgg {

    private final AccountEntity account;

    private final Set<RoleEntity> roles;

    public AccountAgg(AccountEntity account, Collection<RoleEntity> roles) {
        this.account = account;
        this.roles = CollectionUtils.isEmpty(roles) ? null : Sets.newHashSet(roles);
    }

    public AccountEntity getAccount() {
        return account;
    }

    public boolean hasRole() {
        return CollectionUtils.isNotEmpty(this.roles);
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public Optional<List<LegooRole>> getLegooRoles() {
        if(CollectionUtils.isEmpty(this.roles)) return Optional.empty();
        List<LegooRole> list = Lists.newArrayList();
        roles.forEach(x -> list.add(x.toLegooRole()));
        return Optional.of(list);
    }

    public Optional<Set<String>> getRoleNos() {
        if(CollectionUtils.isEmpty(this.roles)) return Optional.empty();
        Set<String> role_no_set = this.roles.stream().map(RoleEntity::getRoleNo).collect(Collectors.toSet());
        return Optional.ofNullable(CollectionUtils.isEmpty(role_no_set) ? null : role_no_set);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("account", account)
                .add("roles", roles)
                .toString();
    }
}
