package com.legooframework.model.regiscenter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.organization.entity.CompanyEntity;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.LocalDate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class DevicePinCodeEntity extends BaseEntity<Long> implements BatchSetter {

    private final Long companyId;
    private final String pinCode;
    private boolean enabled;
    private String deviceId;
    private LocalDate deadline;
    private LocalDate bindingDate;

    DevicePinCodeEntity(CompanyEntity company, String pinCode, Date deadline, LoginContext user) {
        super(-1L, user.getTenantId(), user.getLoginId());
        this.companyId = company.getId();
        Preconditions.checkArgument(Strings.isNotEmpty(pinCode));
        Preconditions.checkArgument(pinCode.length() == 6);
        this.pinCode = pinCode;
        this.enabled = true;
        this.deviceId = null;
        this.deadline = deadline == null ? null : LocalDate.fromDateFields(deadline);
        this.bindingDate = null;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        //(company_id, pin_code, deadline, tenant_id, createUserId, createTime)
        ps.setObject(1, this.companyId);
        ps.setObject(2, this.pinCode);
        ps.setObject(3, this.deadline);
        ps.setObject(4, this.getTenantId());
        ps.setObject(5, this.getCreator());
    }

    public DevicePinCodeEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Long.class);
            this.pinCode = ResultSetUtil.getObject(res, "pinCode", String.class);
            this.enabled = res.getInt("enabled") == 1;
            this.deviceId = ResultSetUtil.getOptString(res, "deviceId", null);
            this.deadline = res.getDate("deadline") == null ? null : LocalDate.fromDateFields(res.getDate("deadline"));
            this.bindingDate = res.getDate("bindingDate") == null ? null : LocalDate.fromDateFields(res.getDate("bindingDate"));
        } catch (SQLException e) {
            throw new RuntimeException("Restore DevicePinCodeEntity has SQLException", e);
        }
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> data = super.toParamMap("enabled", "deadline", "bindingDate");
        data.put("enabled", enabled ? 1 : 0);
        data.put("deadline", deadline == null ? null : deadline.toDate());
        data.put("bindingDate", bindingDate == null ? null : bindingDate.toDate());
        return data;
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

    public Optional<String> getDeviceId() {
        return Optional.ofNullable(deviceId);
    }

    public Optional<LocalDate> getDeadline() {
        return Optional.ofNullable(deadline);
    }

    public Optional<LocalDate> getBindingDate() {
        return Optional.ofNullable(bindingDate);
    }

    public Optional<DevicePinCodeEntity> activeDeviceId(String deviceId) {
        Preconditions.checkArgument(Strings.isNotEmpty(deviceId), "入参 deviceId 不可以为空...");
        if (Objects.equal(this.deviceId, deviceId)) return Optional.empty();
        Preconditions.checkState(Strings.isEmpty(this.deviceId), "该序列号已经被使用 ...");
        Preconditions.checkState(isEnabled(), "该序列号已经被停用 ...");
        Preconditions.checkState(!isDeadlined(), "该序列号已经过期...");
        DevicePinCodeEntity clone = (DevicePinCodeEntity) cloneMe();
        clone.deviceId = deviceId;
        clone.bindingDate = LocalDate.now();
        return Optional.of(clone);
    }

    public boolean isDeadlined() {
        return this.deadline != null && LocalDate.now().isAfter(this.deadline);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevicePinCodeEntity that = (DevicePinCodeEntity) o;
        return enabled == that.enabled &&
                Objects.equal(pinCode, that.pinCode) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(deviceId, that.deviceId) &&
                Objects.equal(deadline, that.deadline) &&
                Objects.equal(bindingDate, that.bindingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, pinCode, enabled, deviceId, deadline, bindingDate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("pinCode", pinCode)
                .add("enabled", enabled)
                .add("deviceId", deviceId)
                .add("deadline", deadline)
                .add("bindingDate", bindingDate)
                .toString();
    }
}
