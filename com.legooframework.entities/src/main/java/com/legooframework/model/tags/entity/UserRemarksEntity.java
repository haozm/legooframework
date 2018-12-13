package com.legooframework.model.tags.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.customer.entity.CustomerId;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class UserRemarksEntity extends BaseEntity<Long> implements BatchSetter {

    private CustomerId customerId;
    private String remarks;
    private String creatorName;

    UserRemarksEntity(Long id, Long tenantId, Long creator, CustomerId customerId, String remarks) {
        super(id, tenantId, creator);
        Preconditions.checkNotNull(customerId);
        this.customerId = customerId;
        setRemarks(remarks);
    }

    public Map<String, Object> toViewData() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("remarksId", getId());
        data.put("creatorName", creatorName);
        data.put("remarks", remarks);
        data.put("remarkTime", getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
        return data;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        INSERT INTO csosm_chat.tag_remarks_record
//                (id, account_id, account_type, store_id, remarks, tenant_id, creator, createTime)
//        VALUES ( ?,          ?,            ?,        ?,       ?,          ?,      ?, NOW())
        ps.setObject(1, getId());
        ps.setObject(2, customerId.getId());
        ps.setObject(3, customerId.getChannel().getVal());
        ps.setObject(4, customerId.getStoreId());
        ps.setObject(5, getRemarks());
        ps.setObject(6, getTenantId());
        ps.setObject(7, super.getCreator());
    }

    UserRemarksEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.customerId = new CustomerId(res);
            this.creatorName = ResultSetUtil.getOptString(res, "creatorName", null);
            setRemarks(ResultSetUtil.getString(res, "remarks"));
        } catch (SQLException e) {
            throw new RuntimeException("Restore CompanyEntity has SQLException", e);
        }
    }

    void setRemarks(String remarks) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarks), "备注信息不可以为空....");
        this.remarks = remarks;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("accountInfo");
        params.putAll(this.customerId.toParamMap());
        return params;
    }

    public Long getStoreId() {
        return customerId.getStoreId();
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserRemarksEntity that = (UserRemarksEntity) o;
        return Objects.equal(customerId, that.customerId) &&
                Objects.equal(remarks, that.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), customerId, remarks);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("customerId", customerId)
                .add("remarks", remarks)
                .toString();
    }
}
