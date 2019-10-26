package com.legooframework.model.regiscenter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TenantNetConfigEntity extends BaseEntity<Long> {

    private final Long companyId;
    private final int port;
    private String webDomain, companyName;

    TenantNetConfigEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Long.class);
            this.port = ResultSetUtil.getObject(res, "port", Integer.class);
            this.webDomain = ResultSetUtil.getObject(res, "webDomain", String.class);
        } catch (SQLException e) {
            throw new RuntimeException("Restore DevicePinCodeEntity has SQLException", e);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("companyId", companyId);
        data.put("companyName", companyName);
        data.put("webDomain", webDomain);
        data.put("port", port);
        return data;
    }

    public String getCompanyName() {
        return companyName;
    }

    void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public int getPort() {
        return port;
    }

    public String getWebDomain() {
        return webDomain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TenantNetConfigEntity that = (TenantNetConfigEntity) o;
        return port == that.port &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(webDomain, that.webDomain);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, port, webDomain);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("port", port)
                .add("webDomain", webDomain)
                .add("companyName", companyName)
                .toString();
    }
}
