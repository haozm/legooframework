package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SMSSettingEntity extends BaseEntity<Integer> {

    private final Integer companyId, storeId;
    private String smsPrefix;

    SMSSettingEntity(Integer companyId, Integer storeId, String smsPrefix) {
        super(0);
        this.companyId = companyId;
        this.storeId = storeId;
        this.smsPrefix = smsPrefix;
    }

    SMSSettingEntity(Integer id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(-1);
            this.smsPrefix = ResultSetUtil.getString(res, "smsPrefix");
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSSettingEntity has SQLException", e);
        }
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public String getSmsPrefix() {
        return smsPrefix;
    }


    public boolean isCompany(CrmStoreEntity store) {
        return this.storeId == -1 && this.companyId.equals(store.getCompanyId());
    }

    boolean isStore(CrmStoreEntity store) {
        return this.companyId.equals(store.getCompanyId()) && this.storeId.equals(store.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSSettingEntity)) return false;
        SMSSettingEntity that = (SMSSettingEntity) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(smsPrefix, that.smsPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, smsPrefix);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("smsPrefix", smsPrefix)
                .toString();
    }
}
