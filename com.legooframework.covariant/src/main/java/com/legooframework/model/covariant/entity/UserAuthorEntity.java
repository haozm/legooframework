package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginUser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserAuthorEntity extends BaseEntity<Integer> implements ToReplace {

    private final static Comparator<Integer> COMPARATOR = Comparator.naturalOrder();
    private String name;
    private List<Integer> roleIds, subOrgIds, subStoreIds;
    private Integer companyId, orgId, storeId;
    private final static Ordering<Integer> ORDER_INT = Ordering.natural();

    UserAuthorEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.name = res.getString("empName");
            this.companyId = res.getInt("comId");
            this.orgId = res.getObject("orgId") == null ? null : res.getInt("orgId");
            this.storeId = res.getObject("storeId") == null ? null : res.getInt("storeId");
            String _roleIds = res.getString("roleIds");
            if (!Strings.isNullOrEmpty(_roleIds)) {
                this.roleIds = ORDER_INT.sortedCopy(Stream.of(StringUtils.split(_roleIds, ',')).map(Integer::parseInt)
                        .collect(Collectors.toList()));
            }
            String _subOrgIds = res.getString("subOrgIds");
            if (!Strings.isNullOrEmpty(_subOrgIds)) {
                this.subOrgIds = ORDER_INT.sortedCopy(Stream.of(StringUtils.split(_subOrgIds, ',')).map(Integer::parseInt)
                        .collect(Collectors.toList()));
            }
            String _storeIds = res.getString("storeIds");
            if (!Strings.isNullOrEmpty(_storeIds)) {
                this.subStoreIds = ORDER_INT.sortedCopy(Stream.of(StringUtils.split(_storeIds, ',')).map(Integer::parseInt)
                        .collect(Collectors.toList()));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore UserAuthorEntity has SQLException", e);
        }
    }

    public LoginContext toLoginContext() {
        return new LoginUser(this.getId().longValue(), this.getCompanyId().longValue(), this.name, "password",
                null, roleIds, storeId, orgId, subStoreIds, null, null);
    }

    public int getMaxRoleId() {
        if (CollectionUtils.isEmpty(this.roleIds)) return -1;
        return this.roleIds.get(0);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Optional<Integer> getOrgId() {
        if (!(isShoppingGuide() || isStoreManager())) return Optional.ofNullable(orgId);
        return Optional.empty();
    }

    public Optional<List<Integer>> getSubOrgIds() {
        if (!(isShoppingGuide() || isStoreManager())) return Optional.ofNullable(subOrgIds);
        return Optional.empty();
    }

    public Optional<List<Integer>> getSubStoreIds() {
        if (!(isShoppingGuide() || isStoreManager())) return Optional.ofNullable(subStoreIds);
        return Optional.empty();
    }

    public Optional<Integer> getStoreId() {
        if (isShoppingGuide() || isStoreManager()) return Optional.ofNullable(storeId);
        return Optional.empty();
    }

    public boolean isStoreManager() {
        return CollectionUtils.isNotEmpty(roleIds) && roleIds.get(0) == 5;
    }

    public boolean isShoppingGuide() {
        return CollectionUtils.isNotEmpty(roleIds) && roleIds.get(0) == 7;
    }

    public boolean isAdmin() {
        return CollectionUtils.isNotEmpty(roleIds) && ArrayUtils.contains(new int[]{1, 2, 3}, roleIds.get(0));
    }

    public boolean isAreaManager() {
        return CollectionUtils.isNotEmpty(roleIds) && ArrayUtils.contains(new int[]{4, 8, 9, 10}, roleIds.get(0));
    }

    public boolean hasStore() {
        return isStoreManager() || isShoppingGuide();
    }

    public boolean hasStores() {
        return CollectionUtils.isNotEmpty(this.subStoreIds);
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("USER_ID", getId());
        map.put("MAX_ROLE", CollectionUtils.isEmpty(roleIds) ? 0 : roleIds.get(0));
        map.put("USER_NAME", this.name);
        map.put("USER_COMPANY_ID", this.companyId);
        map.put("USER_STORE_ID", hasStore() ? storeId : -99);
        map.put("USER_ORG_ID", orgId == null ? -99 : orgId);
        map.put("SUB_STORES", CollectionUtils.isEmpty(subStoreIds) || hasStore() ? new int[0] : subStoreIds);
        map.put("SUB_ORGS", CollectionUtils.isEmpty(subOrgIds) || hasStore() ? new int[0] : subOrgIds);
        map.put("USER_ROLE_IDS", CollectionUtils.isEmpty(roleIds) ? new int[0] : roleIds);
        if (isStoreManager()) {
            map.put("USER_ROLE", "StoreManager");
        } else if (isShoppingGuide()) {
            map.put("USER_ROLE", "ShoppingGuide");
        } else {
            map.put("USER_ROLE", "Boss");
        }
        return map;
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        return this.toViewMap();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("roleIds", roleIds)
                .add("subOrgIds", subOrgIds)
                .add("subStoreIds", subStoreIds)
                .add("companyId", companyId)
                .add("orgId", orgId)
                .add("storeId", storeId)
                .toString();
    }
}
