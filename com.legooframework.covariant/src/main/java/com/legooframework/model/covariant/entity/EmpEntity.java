package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmpEntity extends BaseEntity<Integer> implements ToReplace {

    private Integer loginId;
    private String name, phone;
    private Integer companyId, organizationId, storeId;
    private List<Integer> roleIds;

    EmpEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.loginId = ResultSetUtil.getObject(res, "loginId", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(-1);
            this.organizationId = ResultSetUtil.getOptObject(res, "organizationId", Integer.class).orElse(-1);
            this.phone = ResultSetUtil.getOptString(res, "phone", null);
            this.name = ResultSetUtil.getOptString(res, "name", null);
            String r_ids = res.getString("roleIds");
            if (Strings.isNullOrEmpty(r_ids)) {
                this.roleIds = null;
            } else {
                this.roleIds = Stream.of(StringUtils.split(r_ids, ',')).mapToInt(Integer::parseInt).boxed()
                        .collect(Collectors.toList());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore EmpEntity has SQLException", e);
        }
    }

    public boolean isStoreManager() {
        return CollectionUtils.isNotEmpty(roleIds) && roleIds.contains(5);
    }

    public boolean isShoppingGuide() {
        return CollectionUtils.isNotEmpty(roleIds) && roleIds.contains(7);
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = Maps.newHashMap();
        if (isStoreManager()) {
            params.put("导购姓名", Strings.nullToEmpty(this.name));
            params.put("导购电话", Strings.nullToEmpty(this.phone));
        } else if (isShoppingGuide()) {
            params.put("导购姓名", Strings.nullToEmpty(this.name));
            params.put("导购电话", Strings.nullToEmpty(this.phone));
            params.put("店长姓名", Strings.nullToEmpty(this.name));
            params.put("店长电话", Strings.nullToEmpty(this.phone));
        } else {
            params.put("经理姓名", Strings.nullToEmpty(this.name));
            params.put("经理电话", Strings.nullToEmpty(this.phone));
        }
        params.put("发送人", Strings.nullToEmpty(this.name));
        return params;
    }

    public boolean hasStore() {
        return CollectionUtils.isNotEmpty(roleIds) && (roleIds.contains(5) || roleIds.contains(7));
    }

    public Optional<Integer> getStoreId() {
        return Optional.ofNullable(hasStore() ? storeId : null);
    }


    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("empId", getId());
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EmpEntity empEntity = (EmpEntity) o;
        return Objects.equals(loginId, empEntity.loginId) &&
                Objects.equals(name, empEntity.name) &&
                Objects.equals(phone, empEntity.phone) &&
                Objects.equals(companyId, empEntity.companyId) &&
                Objects.equals(organizationId, empEntity.organizationId) &&
                Objects.equals(storeId, empEntity.storeId) &&
                ListUtils.isEqualList(roleIds, empEntity.roleIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), loginId, name, phone, companyId, organizationId, storeId, roleIds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("loginId", loginId)
                .add("name", name)
                .add("phone", phone)
                .add("companyId", companyId)
                .add("organizationId", organizationId)
                .add("storeId", storeId)
                .add("roleIds", roleIds)
                .toString();
    }
}
