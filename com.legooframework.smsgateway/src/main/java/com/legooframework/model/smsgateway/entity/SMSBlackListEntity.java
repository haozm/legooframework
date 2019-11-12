package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class SMSBlackListEntity extends BaseEntity<String> implements BatchSetter {

    private boolean effective;
    private boolean disable;
    private final Integer companyId, storeId;

    private SMSBlackListEntity(Integer companyId, Integer storeId, String phoneNo, boolean effective, boolean disable) {
        super(phoneNo, -1L, -1L);
        this.effective = effective;
        this.companyId = companyId;
        this.disable = disable;
        this.storeId = storeId == null ? -1 : storeId;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // id, company_id, store_id,is_effective, is_disable
        ps.setObject(1, this.getId());
        ps.setObject(2, this.companyId);
        ps.setObject(3, this.storeId);
        ps.setObject(4, this.effective ? 1 : 0);
        ps.setObject(5, this.disable ? 1 : 0);
    }

    static SMSBlackListEntity effectiveInstance(String phoneNo, boolean effective, Integer memberId, LoginContext user) {
        return null;
        //return new SMSBlackListEntity(phoneNo, effective, false, user.getTenantId().intValue(), memberId, user.getLoginId());
    }

    public static SMSBlackListEntity disableInstance(Integer companyId, Integer storeId, String phoneNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(phoneNo), "电话号码不可以为空值..");
        return new SMSBlackListEntity(companyId, storeId, phoneNo, true, true);
    }

    boolean isEffective() {
        return effective;
    }

    boolean isDisable() {
        return disable;
    }

    Optional<SMSBlackListEntity> uneffective() {
        if (!isEffective()) return Optional.empty();
        SMSBlackListEntity clone = (SMSBlackListEntity) cloneMe();
        clone.effective = false;
        return Optional.of(clone);
    }

    Optional<SMSBlackListEntity> effective() {
        if (isEffective()) return Optional.empty();
        SMSBlackListEntity clone = (SMSBlackListEntity) cloneMe();
        clone.effective = true;
        return Optional.of(clone);
    }

    Optional<SMSBlackListEntity> disabled() {
        if (isDisable()) return Optional.empty();
        SMSBlackListEntity clone = (SMSBlackListEntity) cloneMe();
        clone.disable = true;
        return Optional.of(clone);
    }

    SMSBlackListEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.effective = ResultSetUtil.getBooleanByInt(res, "effective");
            this.disable = ResultSetUtil.getBooleanByInt(res, "disable");
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(-1);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSBlackListEntity has SQLException", e);
        }
    }


    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("companyId", "effective", "disable", "memberId");
        params.put("companyId", companyId);
        params.put("effective", effective ? 1 : 0);
        params.put("disable", disable ? 1 : 0);
        params.put("storeId", storeId);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSBlackListEntity)) return false;
        SMSBlackListEntity that = (SMSBlackListEntity) o;
        return Objects.equal(this.getId(), that.getId()) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId(), effective, disable, companyId, storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("id", getId())
                .add("effective", effective)
                .add("disable", disable)
                .toString();
    }
}
