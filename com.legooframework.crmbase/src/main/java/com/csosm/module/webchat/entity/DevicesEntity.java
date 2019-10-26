package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.*;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

public class DevicesEntity extends BaseEntity<String> {

    private Integer storeId;
    private Integer companyId, status;
    private String employeeName;
    private String weixin;
    private String type;

    DevicesEntity(String deviceId, Integer storeId, Integer companyId, String type, String weixin, Date createtime,
                  int status) {
        super(deviceId, -1, createtime);
        this.storeId = storeId;
        this.companyId = companyId;
        this.weixin = weixin;
        this.type = type;
        this.status = status;
    }

    static DevicesEntity godDevice(String deviceId, StoreEntity store) {
        Preconditions.checkNotNull(store);
        Preconditions.checkState(store.getCompanyId().isPresent());
        DevicesEntity exit = new DevicesEntity(deviceId, store.getId(), store.getCompanyId().get(), "1", null,
                new Date(), 1);
        exit.employeeName = store.getName();
        return exit;
    }

    DevicesEntity disabled() {
        try {
            DevicesEntity _clone = (DevicesEntity) clone();
            _clone.status = 0;
            return _clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    DevicesEntity changeDevice(String deviceId) {
        try {
            DevicesEntity _clone = (DevicesEntity) clone();
            _clone.setId(deviceId);
            return _clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    DevicesEntity merge(DevicesEntity oldDevice) {
        try {
            DevicesEntity _clone = (DevicesEntity) clone();
            _clone.companyId = oldDevice.getCompanyId();
            _clone.storeId = oldDevice.getStoreId();
            return _clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("companyId", companyId);
        data.put("storeId", storeId);
        data.put("deviceId", getId());
        data.put("employeeName", employeeName);
        data.put("deviceType", type);
        data.put("status", status);
        return data;
    }

    public Optional<String> getWeixin() {
        return Optional.fromNullable(weixin);
    }

    public Integer getStoreId() {
        return storeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getType() {
        return type;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isGodDevice() {
        return StringUtils.equals("1", this.type);
    }

    public boolean isAppDevice() {
        return StringUtils.equals("2", this.type);
    }

    public boolean exitsWeiXin() {
        return !Strings.isNullOrEmpty(weixin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DevicesEntity)) return false;
        if (!super.equals(o)) return false;
        DevicesEntity that = (DevicesEntity) o;
        return Objects.equal(storeId, that.storeId) &&
                Objects.equal(type, that.type) &&
                Objects.equal(weixin, that.weixin) &&
                Objects.equal(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), storeId, type, weixin, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("type", type)
                .add("weixin", weixin)
                .toString();
    }
}
