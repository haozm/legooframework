package com.legooframework.model.usergroups.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.customer.entity.Channel;
import com.legooframework.model.organization.entity.EmployeeEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserGroupsByStore {

    private List<UserGroupsEntity> userGroups;
    private Long storeId;
    private Long tenantId;

    UserGroupsByStore(List<UserGroupsEntity> userGroups) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(userGroups));
        this.userGroups = userGroups;
        this.storeId = userGroups.get(0).getStoreId();
        this.tenantId = userGroups.get(0).getTenantId();
    }

    public List<UserGroupsEntity> getUserGroups() {
        return userGroups;
    }

    public Optional<List<UserGroupsEntity>> getByChannel(Channel channel) {
        Preconditions.checkNotNull(channel);
        List<UserGroupsEntity> sublist = this.userGroups.stream().filter(x -> x.getChannel() == channel)
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sublist) ? null : sublist);
    }

    public Optional<List<UserGroupsEntity>> getByEmployee(EmployeeEntity emp) {
        Objects.requireNonNull(emp, "入参emp不能为空");
        List<UserGroupsEntity> sublist = this.userGroups.stream().filter(x -> x.getAuthors().stream()
                .map(a -> a.getId()).collect(Collectors.toSet()).contains(emp.getId()))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sublist) ? null : sublist);
    }

    public boolean exitEmpGroup(UserGroupsEntity entity) {
        if (CollectionUtils.isEmpty(this.userGroups))
            return false;
        Optional<UserGroupsEntity> exits = this.userGroups.stream().filter(x -> x.equalsSameEmpGroup(entity)).findAny();
        return exits.isPresent();
    }

    public boolean exitUserGroup(UserGroupsEntity entity) {
        if (CollectionUtils.isEmpty(this.userGroups))
            return false;
        Optional<UserGroupsEntity> exits = this.userGroups.stream().filter(x -> x.equalsSameUsrGroup(entity)).findAny();
        return exits.isPresent();
    }

    public boolean exitAllFriendGroup(UserGroupsEntity entity) {
        if (CollectionUtils.isEmpty(this.userGroups))
            return false;
        Optional<UserGroupsEntity> exits = this.userGroups.stream().filter(x -> x.equalsSameAllFriendGroup(entity)).findAny();
        return exits.isPresent();
    }

    public Optional<UserGroupsEntity> findById(Long groupId) {
        return this.userGroups.stream().filter(x -> x.getId().equals(groupId)).findFirst();
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public List<UserGroupsEntity> getWechatGroups() {
        return this.userGroups;
    }

    public Optional<List<UserGroupsEntity>> getMemberGroups() {
        return Optional.empty();
    }

    public boolean isNotEmpty() {
        return CollectionUtils.isNotEmpty(this.userGroups);
    }

    public List<UserGroupsEntity> getGroupsByUser(EmployeeEntity employee) {
        Objects.requireNonNull(employee, "入参employee不能为空");
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroupsByStore that = (UserGroupsByStore) o;
        return com.google.common.base.Objects.equal(userGroups, that.userGroups) &&
                com.google.common.base.Objects.equal(storeId, that.storeId) &&
                com.google.common.base.Objects.equal(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(userGroups, storeId, tenantId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userGroups", userGroups)
                .add("storeId", storeId)
                .add("tenantId", tenantId)
                .toString();
    }
}
