package com.legooframework.model.regiscenter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.web.ViewBean;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.LocalDate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class DevicePinCodeEntity extends BaseEntity<Long> implements BatchSetter, ViewBean {

    private final Long companyId;
    private final String pinCode;
    private boolean enabled;
    private String deviceId;
    private Integer storeId;
    private String batchNo;
    private LocalDate bindingDate;
    private PinCodeStauts stauts;

    DevicePinCodeEntity(CrmOrganizationEntity company, String pinCode, String batchNo, LoginContext user) {
        super(-1L, user.getTenantId(), user.getLoginId());
        this.companyId = company.getId().longValue();
        Preconditions.checkArgument(Strings.isNotEmpty(pinCode));
        Preconditions.checkArgument(pinCode.length() == 6);
        this.pinCode = pinCode;
        this.enabled = true;
        this.deviceId = null;
        this.bindingDate = null;
        this.batchNo = batchNo;
        this.stauts = PinCodeStauts.Init;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, this.companyId);
        ps.setObject(2, this.pinCode);
        ps.setObject(3, this.stauts.getStatus());
        ps.setObject(4, this.batchNo);
        ps.setObject(5, this.getTenantId());
        ps.setObject(6, this.getCreator());
    }

    public DevicePinCodeEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Long.class);
            this.pinCode = ResultSetUtil.getObject(res, "pinCode", String.class);
            this.enabled = res.getInt("enabled") == 1;
            this.deviceId = ResultSetUtil.getOptString(res, "deviceId", null);
            this.batchNo = ResultSetUtil.getOptString(res, "batchNo", null);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(null);
            this.bindingDate = res.getDate("bindingDate") == null ? null : LocalDate.fromDateFields(res.getDate("bindingDate"));
            this.stauts = PinCodeStauts.paras(res.getInt("pinCodeStatus"));
        } catch (SQLException e) {
            throw new RuntimeException("Restore DevicePinCodeEntity has SQLException", e);
        }
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> data = super.toParamMap("enabled", "bindingDate", "stauts");
        data.put("enabled", enabled ? 1 : 0);
        data.put("bindingDate", bindingDate == null ? null : bindingDate.toDate());
        data.put("pinCodeStatus", stauts.getStatus());
        return data;
    }

    public PinCodeStauts getStauts() {
        return stauts;
    }

    public boolean isBinding() {
        return Strings.isNotBlank(this.deviceId);
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getPinCode() {
        return pinCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Optional<String> getDeviceId() {
        return Optional.ofNullable(deviceId);
    }

    public Optional<LocalDate> getBindingDate() {
        return Optional.ofNullable(bindingDate);
    }

    Optional<DevicePinCodeEntity> activeDeviceId(String deviceId, CrmStoreEntity store) {
        Preconditions.checkArgument(Strings.isNotEmpty(deviceId), "入参 deviceId 不可以为空...");
        if (Objects.equal(this.deviceId, deviceId)) return Optional.empty();
        Preconditions.checkState(Strings.isEmpty(this.deviceId), "该序列号已经被使用 ...");
        Preconditions.checkState(isEnabled(), "该序列号已经被停用 ...");
        DevicePinCodeEntity clone = (DevicePinCodeEntity) cloneMe();
        clone.deviceId = deviceId;
        clone.storeId = store.getId();
        clone.bindingDate = LocalDate.now();
        clone.stauts = PinCodeStauts.Used;
        return Optional.of(clone);
    }

    Optional<DevicePinCodeEntity> changeDivece(String newDeivceId, CrmStoreEntity store) {
        Preconditions.checkArgument(Strings.isNotEmpty(newDeivceId), "入参 newDeivceId 不可以为空...");
        if (this.deviceId.equals(newDeivceId)) return Optional.empty();
        if (this.storeId != null) Preconditions.checkState(this.storeId.equals(store.getId()));
        DevicePinCodeEntity clone = (DevicePinCodeEntity) cloneMe();
        clone.deviceId = newDeivceId;
        clone.storeId = store.getId();
        clone.stauts = PinCodeStauts.Used;
        return Optional.of(clone);
    }

    Optional<DevicePinCodeEntity> disabled() {
        if (!enabled) return Optional.empty();
        DevicePinCodeEntity clone = (DevicePinCodeEntity) cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    Optional<DevicePinCodeEntity> enabled() {
        if (enabled) return Optional.empty();
        DevicePinCodeEntity clone = (DevicePinCodeEntity) cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("pinCode", pinCode);
        params.put("enabled", enabled);
        params.put("deviceId", deviceId);
        params.put("batchNo", batchNo == null ? "-" : batchNo);
        params.put("stauts", stauts.getStatus());
        params.put("hasBind", Strings.isNotEmpty(deviceId));
        params.put("bindingDate", bindingDate.toString("yyyy-MM-dd"));
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevicePinCodeEntity that = (DevicePinCodeEntity) o;
        return enabled == that.enabled &&
                Objects.equal(pinCode, that.pinCode) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(deviceId, that.deviceId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(bindingDate, that.bindingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, pinCode, enabled, deviceId, storeId, bindingDate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("pinCode", pinCode)
                .add("enabled", enabled)
                .add("deviceId", deviceId)
                .add("storeId", storeId)
                .add("bindingDate", bindingDate)
                .toString();
    }
}
