package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.Map;

public class DeviceNetCfgEntity extends BaseEntity<Long> {

    private final Integer companyId, storeId;
    private String centerId, udpDomain, uploadDomain, companyName, storeName;
    private int updPort, uploadPort, udpPageSize, msgDelayTime, keepliveDelayTime;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        data.put("companyId", companyId);
        data.put("storeId", storeId);
        data.put("centerId", centerId);
        data.put("udpDomain", udpDomain);
        data.put("uploadDomain", uploadDomain);
        data.put("udpPort", updPort);
        data.put("uploadPort", uploadPort);
        data.put("updPageSize", udpPageSize);
        data.put("msgDelayTime", msgDelayTime);
        data.put("keepliveDelayTime", keepliveDelayTime);
        return data;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> data = super.toMap();
        data.put("id", getId());
        data.put("companyId", companyId);
        data.put("storeId", storeId);
        data.put("centerId", centerId);
        data.put("udpDomain", udpDomain);
        data.put("udpPort", updPort);
        data.put("uploadDomain", uploadDomain);
        data.put("uploadPort", uploadPort);
        data.put("udpPageSize", udpPageSize);
        data.put("msgDelayTime", msgDelayTime);
        data.put("keepliveDelayTime", keepliveDelayTime);
        data.put("companyName", companyName);
        data.put("storeName", storeName);
        return data;
    }

    DeviceNetCfgEntity(OrganizationEntity company, String centerId, String udpDomain,
                       String uploadDomain, int updPort, int uploadPort, int udpPageSize, int msgDelayTime,
                       int keepliveDelayTime) {
        super(0L);
        this.companyId = company.getId();
        this.storeId = -1;
        this.centerId = centerId;
        this.udpDomain = udpDomain;
        this.uploadDomain = uploadDomain;
        this.updPort = updPort;
        this.uploadPort = uploadPort;
        this.udpPageSize = udpPageSize;
        this.msgDelayTime = msgDelayTime;
        this.keepliveDelayTime = keepliveDelayTime;
    }

    DeviceNetCfgEntity(StoreEntity store, String centerId, String udpDomain,
                       String uploadDomain, int updPort, int uploadPort, int udpPageSize, int msgDelayTime,
                       int keepliveDelayTime) {
        super(0L);
        this.companyId = store.getCompanyId().or(-1);
        this.storeId = store.getId();
        this.centerId = centerId;
        this.udpDomain = udpDomain;
        this.uploadDomain = uploadDomain;
        this.updPort = updPort;
        this.uploadPort = uploadPort;
        this.udpPageSize = udpPageSize;
        this.msgDelayTime = msgDelayTime;
        this.keepliveDelayTime = keepliveDelayTime;
    }

    DeviceNetCfgEntity(Long id, Integer companyId, Integer storeId, String centerId, String udpDomain,
                       String uploadDomain, int updPort, int uploadPort, int udpPageSize, int msgDelayTime,
                       int keepliveDelayTime, String companyName, String storeName) {
        super(id);
        this.companyId = companyId;
        this.storeId = storeId;
        this.centerId = centerId;
        this.udpDomain = udpDomain;
        this.uploadDomain = uploadDomain;
        this.updPort = updPort;
        this.uploadPort = uploadPort;
        this.udpPageSize = udpPageSize;
        this.msgDelayTime = msgDelayTime;
        this.keepliveDelayTime = keepliveDelayTime;
        this.companyName = companyName;
        this.storeName = storeName;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean hasStore() {
        return -1 != storeId;
    }


    Optional<DeviceNetCfgEntity> change(int udpPageSize, int msgDelayTime, int keepliveDelayTime) {
        Preconditions.checkState(udpPageSize > 0, "非法的入参 udpPageSize = %s", udpPageSize);
        Preconditions.checkState(msgDelayTime > 0, "非法的入参 msgDelayTime = %s", msgDelayTime);
        Preconditions.checkState(keepliveDelayTime > 0, "非法的入参 keepliveDelayTime = %s", keepliveDelayTime);
        try {
            DeviceNetCfgEntity clone = (DeviceNetCfgEntity) this.clone();
            clone.udpPageSize = udpPageSize;
            clone.msgDelayTime = msgDelayTime;
            clone.keepliveDelayTime = keepliveDelayTime;
            if (this.equals(clone)) return Optional.absent();
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getStoreId() {
        return storeId;
    }

    public String getCenterId() {
        return centerId;
    }

    public String getUdpDomain() {
        return udpDomain;
    }

    public String getUploadDomain() {
        return uploadDomain;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getStoreName() {
        return storeName;
    }

    public int getUpdPort() {
        return updPort;
    }

    public int getUploadPort() {
        return uploadPort;
    }

    public int getUdpPageSize() {
        return udpPageSize;
    }

    public int getMsgDelayTime() {
        return msgDelayTime;
    }

    public int getKeepliveDelayTime() {
        return keepliveDelayTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceNetCfgEntity)) return false;
        DeviceNetCfgEntity that = (DeviceNetCfgEntity) o;
        return updPort == that.updPort &&
                uploadPort == that.uploadPort &&
                udpPageSize == that.udpPageSize &&
                msgDelayTime == that.msgDelayTime &&
                keepliveDelayTime == that.keepliveDelayTime &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(centerId, that.centerId) &&
                Objects.equal(udpDomain, that.udpDomain) &&
                Objects.equal(uploadDomain, that.uploadDomain);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, centerId, udpDomain, uploadDomain,
                updPort, uploadPort, udpPageSize, msgDelayTime, keepliveDelayTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("centerId", centerId)
                .add("udpDomain", udpDomain)
                .add("uploadDomain", uploadDomain)
                .add("updPort", updPort)
                .add("uploadPort", uploadPort)
                .add("udpPageSize", udpPageSize)
                .add("msgDelayTime", msgDelayTime)
                .add("keepliveDelayTime", keepliveDelayTime)
                .toString();
    }
}
