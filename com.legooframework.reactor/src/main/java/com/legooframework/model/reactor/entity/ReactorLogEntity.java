package com.legooframework.model.reactor.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class ReactorLogEntity extends BaseEntity<Long> implements BatchSetter {

    private final Integer companyId, storeId, orgId;
    private final String sourceId, sourceTable, errorCode, errorMsg, beforeCtx, afterCtx;
    private final Long templateId;

    ReactorLogEntity(Integer companyId, Integer storeId, Integer orgId, String sourceId, String sourceTable,
                     String errorCode, String errorMsg, String beforeCtx, String afterCtx, Long templateId) {
        super(0L);
        this.companyId = companyId;
        this.storeId = storeId;
        this.orgId = orgId;
        this.sourceId = sourceId;
        this.sourceTable = sourceTable;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.beforeCtx = beforeCtx;
        this.afterCtx = afterCtx;
        this.templateId = templateId;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, this.companyId);
        ps.setObject(2, this.orgId);
        ps.setObject(3, this.storeId);
        ps.setObject(4, this.sourceId);
        ps.setObject(5, this.sourceTable);
        ps.setObject(6, this.templateId);
        ps.setObject(7, this.errorCode);
        ps.setObject(8, this.beforeCtx);
        ps.setObject(9, this.afterCtx);
        ps.setObject(10, this.errorMsg);
        ps.setObject(11, this.companyId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactorLogEntity that = (ReactorLogEntity) o;
        return Objects.equals(companyId, that.companyId) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(orgId, that.orgId) &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(sourceTable, that.sourceTable) &&
                Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(errorMsg, that.errorMsg) &&
                Objects.equals(beforeCtx, that.beforeCtx) &&
                Objects.equals(afterCtx, that.afterCtx) &&
                Objects.equals(templateId, that.templateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, storeId, orgId, sourceId, sourceTable, errorCode, errorMsg, beforeCtx, afterCtx, templateId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("orgId", orgId)
                .add("sourceId", sourceId)
                .add("sourceTable", sourceTable)
                .add("errorCode", errorCode)
                .add("errorMsg", errorMsg)
                .add("beforeCtx", beforeCtx)
                .add("afterCtx", afterCtx)
                .add("templateId", templateId)
                .toString();
    }
}
