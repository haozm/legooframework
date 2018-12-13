package com.legooframework.model.organization.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.devices.dto.DeviceDto;
import com.legooframework.model.devices.entity.DeviceEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

// 机构设备表
public class EquipmentEntity extends BaseEntity<String> {

    private final DeviceDto device;
    private String deviceType;
    private String deviceTypeName;
    public final static String DEVICETYPE_DICT = "DEVICETYPE";
    private boolean enabled;
    public String remark, enabledName;
    public Long storeId, companyId;
    private Date activateDate;
    private String storeFullName;

    private EquipmentEntity(DeviceEntity device, String deviceType, boolean enabled, String remark, CompanyEntity company) {
        super(device.getId(), company.getId(), -1L);
        this.device = device.createDto();
        this.deviceType = deviceType;
        this.enabled = enabled;
        this.activateDate = null;
        this.remark = remark;
        this.companyId = company.getId();
    }

    Optional<EquipmentEntity> enabled() {
        if (isEnabled()) return Optional.empty();
        EquipmentEntity clone = (EquipmentEntity) super.cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    Optional<EquipmentEntity> disabled() {
        if (!isEnabled()) return Optional.empty();
        EquipmentEntity clone = (EquipmentEntity) super.cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    static EquipmentEntity createMainDev(DeviceEntity device, String remark, CompanyEntity company) {
        return new EquipmentEntity(device, "X", true, remark, company);
    }

    public void canBindingToStore() {
        Preconditions.checkState(device.isNormalState(), "当前设备物理状态为%s，无法进行绑定.",
                device.getState().getName());
        Preconditions.checkState(isEnabled(), "当前设备处于停用状态，无法绑定门店.");
    }

    public void canLogining() {
        Preconditions.checkState(device.isNormalState(), "当前设备物理状态为%s，禁止登陆.",
                device.getState().getName());
        Preconditions.checkState(isEnabled(), "当前设备状态%s，禁止登陆.", this.enabledName);
        Preconditions.checkState(isAssigned(), "当前设备尚未分配到门店，禁止登陆");
    }

    static EquipmentEntity createAppDev(DeviceEntity device, String remark, CompanyEntity company) {
        return new EquipmentEntity(device, "A", true, remark, company);
    }

    EquipmentEntity(String id, ResultSet res) {
        super(id);
        try {
            this.device = new DeviceDto(ResultSetUtil.getString(res, "deviceId"),
                    ResultSetUtil.getString(res, "deviceImei"),
                    ResultSetUtil.getString(res, "deviceName"),
                    res.getInt("deviceState"));
            //deviceAssign
            this.deviceType = ResultSetUtil.getString(res, "deviceType");
            this.deviceTypeName = ResultSetUtil.getString(res, "deviceTypeName");
            this.enabled = ResultSetUtil.getBooleanByInt(res, "deviceEnabled");
            this.enabledName = this.enabled ? "启用" : "停用";
            this.companyId = ResultSetUtil.getObject(res, "companyId", Long.class);
            this.activateDate = ResultSetUtil.getOptObject(res, "activateDate", Date.class).orElse(null);
            String store_info = ResultSetUtil.getOptString(res, "storeInfo", null);
            if (Strings.isNullOrEmpty(store_info)) {
                this.storeId = null;
                this.storeFullName = null;
            } else {
                String[] args = StringUtils.split(store_info, "[@@]");
                this.storeId = Long.valueOf(args[0]);
                this.storeFullName = args.length == 2 ? args[1] : "某门店";
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore EquipmentEntity has SQLException", e);
        }
    }

    public String getStoreFullName() {
        return storeFullName;
    }

    public Optional<Long> getStoreId() {
        return Optional.ofNullable(this.storeId);
    }

    public boolean isActivated() {
        return null != activateDate;
    }

    public Date getActivateDate() {
        return activateDate;
    }

    public String getEnabledName() {
        return enabledName;
    }

    public boolean isXDevice() {
        return StringUtils.equals("X", this.deviceType);
    }

    public boolean isAppDevice() {
        return StringUtils.equals("A", this.deviceType);
    }

    public DeviceDto getDevice() {
        return device;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public String getRemark() {
        return remark;
    }

    public boolean isAssigned() {
        return null != this.storeId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquipmentEntity)) return false;
        if (!super.equals(o)) return false;
        EquipmentEntity that = (EquipmentEntity) o;
        return enabled == that.enabled &&
                Objects.equal(device, that.device) &&
                DateUtils.isSameDay(activateDate, that.activateDate) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(deviceType, that.deviceType) &&
                Objects.equal(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, device, deviceType, enabled, activateDate, remark);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("deviceType", deviceType)
                .add("deviceTypeName", deviceTypeName)
                .add("assigned", isAssigned())
                .add("storeId", storeId)
                .add("storeFullName", storeFullName)
                .add("enabled", enabled)
                .add("enabledName", enabledName)
                .add("device", device)
                .add("companyId", companyId)
                .add("remark", remark)
                .toString();
    }
}
