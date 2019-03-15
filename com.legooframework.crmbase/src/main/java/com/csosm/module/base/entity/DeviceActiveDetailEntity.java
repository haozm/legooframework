package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

public class DeviceActiveDetailEntity extends BaseEntity<Long> {

    private final Integer companyId;
    private final String pinCode;
    private final Integer storeId;
    private boolean enabled;
    private String deviceId;
    private Date bindDate;

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        data.put("companyId", companyId);
        data.put("pinCode", pinCode);
        data.put("storeId", storeId);
        data.put("deviceId", deviceId);
        data.put("enabled", enabled ? 1 : 0);
        data.put("bindDate", bindDate);
        return data;
    }

    DeviceActiveDetailEntity(StoreEntity store, String pinCode, String deviceId) {
        super(0L);
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(store.getCompanyId().isPresent());
        this.companyId = store.getCompanyId().get();
        this.storeId = store.getId();
        this.deviceId = deviceId;
        this.bindDate = new Date();
        this.pinCode = pinCode;
    }

    DeviceActiveDetailEntity(Long id, Object createUserId, Date createTime, Integer companyId, String pinCode,
                             Integer storeId, boolean enabled, String deviceId, Date bildDate) {
        super(id, createUserId, createTime);
        this.companyId = companyId;
        this.pinCode = pinCode;
        this.storeId = storeId;
        this.enabled = enabled;
        this.deviceId = "NO_DEVICE".equals(deviceId) ? null : deviceId;
        this.bindDate = bildDate;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getPinCode() {
        return pinCode;
    }


    public Integer getStoreId() {
        return storeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Date getBindDate() {
        return bindDate;
    }

    boolean equals(StoreEntity store, String deviceId) {
        return this.storeId.equals(store.getId()) && StringUtils.equals(this.deviceId, deviceId);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeviceActiveDetailEntity that = (DeviceActiveDetailEntity) o;
        return enabled == that.enabled &&
                Objects.equal(pinCode, that.pinCode) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, pinCode, storeId, enabled, deviceId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("pinCode", pinCode)
                .add("storeId", storeId)
                .add("enabled", enabled)
                .add("deviceId", deviceId)
                .add("bindDate", bindDate)
                .toString();
    }
}
