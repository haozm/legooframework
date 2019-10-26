package com.legooframework.model.covariant.entity;


import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StoEntity extends BaseEntity<Integer> implements ToReplace {

    private final String name, phone, deviceId, weixinId, address;
    private final Integer companyId, orgId;

    StoEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.companyId = ResultSetUtil.getObject(res, "company_id", Integer.class);
            this.orgId = ResultSetUtil.getObject(res, "organization_id", Integer.class);
            this.name = ResultSetUtil.getObject(res, "name", String.class);
            this.phone = ResultSetUtil.getOptString(res, "phone", null);
            this.deviceId = ResultSetUtil.getOptString(res, "deviceid", null);
            this.weixinId = ResultSetUtil.getOptString(res, "weixin", null);
            this.address = ResultSetUtil.getOptString(res, "detailAddress", null);
            // detailAddress
        } catch (SQLException e) {
            throw new RuntimeException("Restore StoEntity has SQLException", e);
        }
    }

    public Optional<String> getWeixinId() {
        return Optional.ofNullable(weixinId);
    }

    public boolean hasWexin() {
        return deviceId != null && weixinId != null;
    }

    public String loadDeviceId() {
        Preconditions.checkState(!Strings.isNullOrEmpty(deviceId), "门店 %s 无设备信息...", this.name);
        return deviceId;
    }

    public String loadWeixinId() {
        Preconditions.checkState(!Strings.isNullOrEmpty(weixinId), "门店 %s 无微信信息...", this.name);
        return weixinId;
    }

    Optional<String> getDeviceId() {
        return Optional.ofNullable(deviceId);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("门店名称", Strings.nullToEmpty(this.name));
        params.put("门店电话", Strings.nullToEmpty(this.phone));
        params.put("门店地址", Strings.nullToEmpty(this.address));
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoEntity stoEntity = (StoEntity) o;
        return Objects.equals(name, stoEntity.name) &&
                Objects.equals(getId(), stoEntity.getId()) &&
                Objects.equals(companyId, stoEntity.companyId) &&
                Objects.equals(orgId, stoEntity.orgId);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("orgId", orgId);
        params.put("storeId", getId());
        params.put("deviceId", deviceId);
        params.put("weixinId", weixinId);
        return params;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), name, companyId, orgId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("companyId", companyId)
                .add("orgId", orgId)
                .add("deviceId", deviceId)
                .add("weixinId", weixinId)
                .toString();
    }
}
