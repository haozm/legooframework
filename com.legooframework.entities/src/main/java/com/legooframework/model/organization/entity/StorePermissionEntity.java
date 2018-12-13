package com.legooframework.model.organization.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StorePermissionEntity extends BaseEntity<Long> {

    private Long employeeId;
    private Set<Long> orgIds, storeIds;

    StorePermissionEntity(Long id, LoginContext loginUser, EmployeeEntity employee,
                          Collection<StoreTreeEntity> orgs, Collection<StoreEntity> stores) {
        super(id, loginUser.getTenantId(), loginUser.getLoginId());
        Preconditions.checkNotNull(employee, "职员 employee 不可以为空...");
        this.employeeId = employee.getId();
        this.orgIds = CollectionUtils.isEmpty(orgs) ? null :
                orgs.stream().map(StoreTreeEntity::getId).collect(Collectors.toSet());
        this.storeIds = CollectionUtils.isEmpty(stores) ? null :
                stores.stream().map(StoreEntity::getId).collect(Collectors.toSet());
    }

    StorePermissionEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.employeeId = ResultSetUtil.getObject(res, "employeeId", Long.class);
            Optional<String> _orgIds = ResultSetUtil.getOptObject(res, "orgIds", String.class);
            _orgIds.ifPresent(s -> this.orgIds = Stream.of(StringUtils.split(s, ','))
                    .map(Long::valueOf).collect(Collectors.toSet()));
            Optional<String> _storeIds = ResultSetUtil.getOptObject(res, "storeIds", String.class);
            _storeIds.ifPresent(s -> this.storeIds = Stream.of(StringUtils.split(s, ','))
                    .map(Long::valueOf).collect(Collectors.toSet()));
        } catch (SQLException e) {
            throw new RuntimeException("Restore StorePermissionEntity has SQLException", e);
        }
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> data = super.toParamMap("orgIds", "storeIds");
        data.put("storeIds", CollectionUtils.isEmpty(this.storeIds) ? null : Joiner.on(',').join(this.storeIds));
        data.put("orgIds", CollectionUtils.isEmpty(this.orgIds) ? null : Joiner.on(',').join(this.orgIds));
        return data;
    }

    Optional<StorePermissionEntity> changePermission(Collection<StoreEntity> stores, Collection<StoreTreeEntity> orgs) {
        Set<Long> store_ids = CollectionUtils.isEmpty(stores) ? null :
                stores.stream().map(StoreEntity::getId).collect(Collectors.toSet());
        Set<Long> org_ids = CollectionUtils.isEmpty(orgs) ? null :
                orgs.stream().map(StoreTreeEntity::getId).collect(Collectors.toSet());
        StorePermissionEntity clone = (StorePermissionEntity) cloneMe();
        clone.storeIds = store_ids;
        clone.orgIds = org_ids;
        if (this.equals(clone)) return Optional.empty();
        return Optional.of(clone);
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public boolean exitsOrgs() {
        return CollectionUtils.isNotEmpty(this.orgIds);
    }

    public boolean exitsStores() {
        return CollectionUtils.isNotEmpty(this.storeIds);
    }

    public Set<Long> getOrgIds() {
        Preconditions.checkState(exitsOrgs());
        return ImmutableSet.copyOf(orgIds);
    }

    public Set<Long> getStoreIds() {
        Preconditions.checkState(exitsStores());
        return ImmutableSet.copyOf(storeIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StorePermissionEntity that = (StorePermissionEntity) o;
        return employeeId.equals(that.employeeId) &&
                SetUtils.isEqualSet(orgIds, that.orgIds) &&
                SetUtils.isEqualSet(storeIds, that.storeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employeeId, orgIds, storeIds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("employeeId", employeeId)
                .add("orgIds", orgIds)
                .add("storeIds", storeIds)
                .toString();
    }

}
