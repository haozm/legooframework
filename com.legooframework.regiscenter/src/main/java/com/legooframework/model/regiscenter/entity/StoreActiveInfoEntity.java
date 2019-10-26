package com.legooframework.model.regiscenter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.joda.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class StoreActiveInfoEntity extends BaseEntity<Long> {

    private Integer companyId, storeId;
    private LocalDate activeDate, deadline;
    private String deviceId;
    private boolean expired;

    StoreActiveInfoEntity(CrmStoreEntity store, LocalDate activeDate, String deviceId) {
        super(0L, store.getCompanyId().longValue(), -1L);
        this.companyId = store.getCompanyId();
        this.storeId = store.getId();
        this.activeDate = activeDate;
        this.deviceId = deviceId;
        this.deadline = activeDate.plusYears(1);
        this.expired = false;
    }

    StoreActiveInfoEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.activeDate = LocalDate.fromDateFields(res.getDate("activeDate"));
            this.deviceId = ResultSetUtil.getOptString(res, "deviceId", null);
            this.deadline = LocalDate.fromDateFields(res.getDate("deadline"));
            this.expired = LocalDate.now().isAfter(deadline);
        } catch (SQLException e) {
            throw new RuntimeException("Restore StoreActiveInfoEntity has SQLException", e);
        }
    }


    public boolean isExpired() {
        return expired;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    boolean sameStore(CrmStoreEntity store) {
        return this.storeId.equals(store.getId()) && this.companyId.equals(store.getCompanyId());
    }

    Optional<StoreActiveInfoEntity> changeDevice(String newDeivceId) {
        if (this.deviceId.equals(newDeivceId)) return Optional.empty();
        StoreActiveInfoEntity clone = (StoreActiveInfoEntity) cloneMe();
        clone.deviceId = newDeivceId;
        return Optional.of(clone);
    }

    public Integer getStoreId() {
        return storeId;
    }

    public LocalDate getActiveDate() {
        return activeDate;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> param = super.toParamMap("activeDate", "deadline");
        param.put("activeDate", activeDate.toDate());
        param.put("deadline", deadline == null ? null : deadline.toDate());
        return param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoreActiveInfoEntity)) return false;
        StoreActiveInfoEntity that = (StoreActiveInfoEntity) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, deviceId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("activeDate", activeDate)
                .add("deviceId", deviceId)
                .toString();
    }
}
