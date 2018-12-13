package com.legooframework.model.security.event;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.event.LegooEvent;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

public class SecModuleEvent extends LegooEvent {

    SecModuleEvent(String source, String eventName) {
        super(source, "security", eventName);
    }

    void setAccountNo(String accountNo) {
        super.putPayload("accountNo", accountNo);
    }

    public String getAccountNo() {
        return super.getString("accountNo");
    }

    void setAccountName(String accountName) {
        super.putPayload("accountName", accountName);
    }

    public String getAccountName() {
        return super.getString("accountName");
    }

    void setPassword(String password) {
        super.putPayload("password", password);
    }

    void setRoleNos(String... roleNos) {
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(roleNos), "(String... roleNos)角色编号列表不可以为空.");
        super.putPayload("roleNos", roleNos);
    }

    public String[] getRoleNos() {
        return (String[]) super.getPayload().get("roleNos");
    }

    public String getPassword() {
        return super.getString("password");
    }

    void setAccountId(Long id) {
        super.putPayload("accountId", id);
    }

    void setTenantId(Long tenantId) {
        super.putPayload("tenantId", tenantId);
    }

    public Long getAccountId() {
        return MapUtils.getLong(super.payload, "accountId");
    }

    public Long getTenantId() {
        return MapUtils.getLong(super.payload, "tenantId");
    }
}
