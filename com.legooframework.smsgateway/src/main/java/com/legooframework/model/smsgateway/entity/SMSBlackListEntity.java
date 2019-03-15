package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
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
    private final Integer companyId, memberId;

    private SMSBlackListEntity(String phoneNo, boolean effective, boolean disable, Integer companyId, Integer memberId, long userId) {
        super(phoneNo, companyId.longValue(), userId);
        this.effective = effective;
        this.companyId = companyId;
        this.disable = disable;
        this.memberId = memberId;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // id, company_id, is_effective, is_disable, member_id,  tenant_id, creator
        ps.setObject(1, this.getId());
        ps.setObject(2, this.companyId);
        ps.setObject(3, this.effective ? 1 : 0);
        ps.setObject(4, this.disable ? 1 : 0);
        ps.setObject(5, memberId);
        ps.setObject(6, this.companyId);
        ps.setObject(7, this.getCreator());
    }

    static SMSBlackListEntity effectiveInstance(String phoneNo, boolean effective, Integer memberId, LoginContext user) {
        return new SMSBlackListEntity(phoneNo, effective, false, user.getTenantId().intValue(), memberId, user.getLoginId());
    }

    static SMSBlackListEntity disableInstance(String phoneNo, boolean disable, Integer companyId) {
        return new SMSBlackListEntity(phoneNo, true, disable, companyId, -1, -1L);
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
            this.memberId = ResultSetUtil.getOptObject(res, "memberId", Integer.class).orElse(null);
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
        params.put("memberId", memberId);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSBlackListEntity)) return false;
        if (!super.equals(o)) return false;
        SMSBlackListEntity that = (SMSBlackListEntity) o;
        return effective == that.effective &&
                disable == that.disable &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), effective, disable, companyId, memberId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("effective", effective)
                .add("disable", disable)
                .add("companyId", companyId)
                .add("memberId", memberId)
                .toString();
    }
}
