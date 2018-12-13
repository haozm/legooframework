package com.legooframework.model.crmadapter.entity;


import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class CrmStoreEntity extends BaseEntity<Integer> {

    private String name, address, oldStoreId, deviceId;
    private Integer status;
    private Integer organizationId;
    private Integer companyId;

    CrmStoreEntity(Integer id, ResultSet res) {
        super(id, res);
        try {
            this.organizationId = ResultSetUtil.getOptObject(res, "organizationId", Integer.class).orElse(null);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.name = ResultSetUtil.getOptString(res, "name", null);
            this.address = ResultSetUtil.getOptString(res, "address", null);
            this.oldStoreId = ResultSetUtil.getOptString(res, "oldStoreId", null);
            this.deviceId = ResultSetUtil.getOptString(res, "deviceId", null);
            this.status = ResultSetUtil.getObject(res, "status", Integer.class);
        } catch (SQLException e) {
            throw new RuntimeException("Restore CrmStoreEntity has SQLException", e);
        }
    }

    public boolean isEnabled() {
        return null != status && 1 == this.status;
    }

    public Optional<String> getDeviceId() {
        return Optional.ofNullable(deviceId);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getOldStoreId() {
        return Optional.ofNullable(oldStoreId);
    }

    public String getAddress() {
        return address;
    }

    public Optional<Integer> getOrganizationId() {
        return Optional.ofNullable(organizationId);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isCrossStore(CrmMemberEntity member) {
        return !member.getStoreId().equals(this.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrmStoreEntity that = (CrmStoreEntity) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(oldStoreId, that.oldStoreId) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, address, oldStoreId, deviceId, status, organizationId,
                companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("address", address)
                .add("oldStoreId", oldStoreId)
                .add("deviceId", deviceId)
                .add("status", status)
                .add("organizationId", organizationId)
                .add("companyId", companyId)
                .toString();
    }
}
