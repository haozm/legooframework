package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public abstract class UseRangeEntity extends BaseEntity<Long> {

    private final Integer storeId, companyId;
    private boolean enabled;

    protected UseRangeEntity(OrgEntity company) {
        super(0L, DateTime.now());
        this.companyId = company.getId();
        this.storeId = 0;
        this.enabled = true;
    }

    protected UseRangeEntity(StoEntity store) {
        super(0L, DateTime.now());
        this.companyId = store.getCompanyId();
        this.storeId = store.getId();
        this.enabled = true;
    }

    protected UseRangeEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = res.getInt("companyId");
            this.storeId = res.getInt("storeId");
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
        } catch (SQLException e) {
            throw new RuntimeException("Restore UseRangeEntity has SQLException", e);
        }
    }

    Integer getCompanyId() {
        return companyId;
    }

    boolean isEnabled() {
        return enabled;
    }

    boolean isComRange() {
        return this.storeId == 0;
    }

    boolean isStoreRange() {
        return this.storeId != 0;
    }

    boolean isOwnStore(StoEntity store) {
        return isStoreRange() && this.companyId.equals(store.getCompanyId()) && this.storeId.equals(store.getId());
    }

    boolean isStoreWithCompany(StoEntity store) {
        return (isComRange() && this.companyId.equals(store.getCompanyId())) || isOwnStore(store);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UseRangeEntity that = (UseRangeEntity) o;
        return enabled == that.enabled &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), storeId, companyId, enabled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("enabled", enabled)
                .toString();
    }
}
