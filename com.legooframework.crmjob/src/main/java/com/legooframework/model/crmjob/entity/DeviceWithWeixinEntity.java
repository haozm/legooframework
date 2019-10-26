package com.legooframework.model.crmjob.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.DataRange;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceWithWeixinEntity extends BaseEntity<Integer> implements DataRange {

    private String deviceId;
    private String weixinId;
    private Integer companyId, storeId;

    DeviceWithWeixinEntity(ResultSet res) {
        super(0);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Long.class).intValue();
            this.storeId = ResultSetUtil.getObject(res, "storeId", Long.class).intValue();
            this.weixinId = ResultSetUtil.getString(res, "weixinId");
            this.deviceId = ResultSetUtil.getString(res, "deviceId");
        } catch (SQLException e) {
            throw new RuntimeException("Restore DeviceWithWeixinEntity has SQLException", e);
        }
    }

    boolean isCompany(Integer companyId) {
        return Objects.equal(this.companyId, companyId);
    }

    boolean isStore(Integer companyId, Integer storeId) {
        return Objects.equal(this.companyId, companyId) && Objects.equal(this.storeId, storeId);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getWeixinId() {
        return weixinId;
    }

    @Override
    public Integer getCompanyId() {
        return companyId;
    }

    @Override
    public Integer getStoreId() {
        return storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceWithWeixinEntity that = (DeviceWithWeixinEntity) o;
        return Objects.equal(deviceId, that.deviceId) &&
                Objects.equal(weixinId, that.weixinId) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(deviceId, weixinId, companyId, storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("deviceId", deviceId)
                .add("weixinId", weixinId)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .toString();
    }
}
