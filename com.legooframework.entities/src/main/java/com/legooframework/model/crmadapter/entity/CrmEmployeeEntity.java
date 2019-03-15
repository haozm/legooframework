package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class CrmEmployeeEntity extends BaseEntity<Integer> {

    private Integer companyId, orgId, storeId;
    private String userName, password;
    private List<CrmRole> roles;
    private static Ordering<CrmRole> ORDERING = Ordering.natural()
            .onResultOf((Function<CrmRole, Integer>) crmRole -> crmRole != null ? crmRole.getPower() : 0);

    CrmEmployeeEntity(Integer id, Integer companyId, Integer orgId, Integer storeId,
                      String userName, String roleIds, String password) {
        super(id);
        this.companyId = companyId;
        this.orgId = orgId;
        this.storeId = storeId;
        this.password = password;
        this.userName = userName;
        if (!Strings.isNullOrEmpty(roleIds)) {
            List<CrmRole> _roles = Lists.newArrayList();
            Stream.of(StringUtils.split(roleIds, ',')).forEach(x -> {
                _roles.add(CrmRole.parse(Integer.valueOf(x)));
            });
            this.roles = ImmutableList.copyOf(ORDERING.sortedCopy(_roles));
        } else {
            this.roles = null;
        }
    }

    public boolean isOnlyStore() {
        if (CollectionUtils.isEmpty(roles)) return false;
        return roles.get(roles.size() - 1).equals(CrmRole.ShoppingGuideRole) ||
                roles.get(roles.size() - 1).equals(CrmRole.StoreManagerRole);
    }

    public boolean hasStoreManager() {
        return CollectionUtils.isNotEmpty(roles) && roles.contains(CrmRole.StoreManagerRole);
    }

    public boolean hasDaogou() {
        return CollectionUtils.isNotEmpty(roles) && roles.contains(CrmRole.ShoppingGuideRole);
    }


    public Integer getComId() {
        return companyId;
    }

    public Optional<Integer> getOrgId() {
        return Optional.ofNullable(orgId);
    }

    public Integer getCompanyId() {
        return companyId;
    }


    public Optional<Integer> getStoreId() {
        return Optional.ofNullable(storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("userName", userName)
                .add("roleIds", roles)
                .toString();
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrmEmployeeEntity that = (CrmEmployeeEntity) o;
        return Objects.equals(companyId, that.companyId) &&
                Objects.equals(orgId, that.orgId) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(roles, that.roles);
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), companyId, orgId, storeId, userName, roles);
    }
}
