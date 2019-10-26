package com.legooframework.model.autotask.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.DataType;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TaskSourceEntity extends BaseEntity<Long> {

    private final BusinessType businessType;
    // payloadType 0:json  1:map
    private final DataType payloadType;
    private final Integer companyId, orgId, storeId, employeeId, memberId;
    private final String openid, weixinId;
    private int status;
    private String message, payload;
    private final LocalDateTime generationTime;

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("sourceId", getId());
        params.put("businessType", businessType.getValue());
        params.put("status", status);
        params.put("openid", openid);
        params.put("weixinId", weixinId);
        params.put("employeeId", employeeId);
        params.put("memberId", memberId);
        params.put("orgId", orgId);
        params.put("payloadType", payloadType);
        return params;
    }

    TaskSourceEntity(Long id, ResultSet res) {
        super(id);
        try {
            this.businessType = BusinessType.paras(res.getInt("business_type"));
            this.status = res.getInt("status");
            this.payloadType = DataType.paras(res.getInt("payload_type"));
            this.companyId = ResultSetUtil.getObject(res, "company_id", Integer.class);
            this.orgId = res.getInt("org_id") == 0 ? null : res.getInt("org_id");
            this.storeId = res.getInt("store_id") == 0 ? null : res.getInt("store_id");
            this.employeeId = res.getInt("employee_id") == 0 ? null : res.getInt("employee_id");
            this.memberId = res.getInt("member_id") == 0 ? null : res.getInt("member_id");
            this.openid = ResultSetUtil.getOptString(res, "openid", null);
            this.weixinId = ResultSetUtil.getOptString(res, "weixin_id", null);
            this.generationTime = ResultSetUtil.getLocalDateTime(res, "generation_time");
            this.message = ResultSetUtil.getOptString(res, "message", null);
            this.payload = ResultSetUtil.getOptString(res, "payload", null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore TaskSourceEntity has SQLException", e);
        }
    }

    Optional<String> getPayload() {
        return Optional.ofNullable(Strings.emptyToNull(payload));
    }

    boolean isTodo() {
        return this.status == 0;
    }

    BusinessType getBusinessType() {
        return businessType;
    }

    boolean isFinish() {
        return this.status == 1 || this.status == 9;
    }

    boolean isError() {
        return this.status == 9;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Optional<Integer> getOrgId() {
        return Optional.ofNullable(orgId);
    }

    public Optional<Integer> getStoreId() {
        return Optional.ofNullable(storeId);
    }

    public Optional<Integer> getEmployeeId() {
        return Optional.ofNullable(employeeId);
    }

    public Optional<Integer> getMemberId() {
        return Optional.ofNullable(memberId);
    }

    public Optional<String> getWeixinId() {
        return Optional.ofNullable(weixinId);
    }

    LocalDateTime getGenerationTime() {
        return generationTime;
    }

    public void error(String errMsg) {
        Preconditions.checkState(this.status == 0);
        this.status = 9;
        this.message = errMsg;
    }

    int getStatus() {
        return status;
    }

    String getMessage() {
        return message;
    }

    void finshed(String message) {
        this.status = 1;
        this.message = message;
    }

    public void error4NoRule() {
        this.status = 9;
        this.message = "未设置规则";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskSourceEntity that = (TaskSourceEntity) o;
        return businessType == that.businessType &&
                payloadType == that.payloadType &&
                status == that.status &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(orgId, that.orgId) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(memberId, that.memberId) &&
                Objects.equals(openid, that.openid) &&
                Objects.equals(weixinId, that.weixinId) &&
                Objects.equals(message, that.message) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.getId(), businessType, payloadType, companyId, orgId, storeId, employeeId,
                memberId, openid, weixinId, status, message, payload);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("businessType", businessType)
                .add("payloadType", payloadType)
                .add("companyId", companyId)
                .add("orgId", orgId)
                .add("storeId", storeId)
                .add("employeeId", employeeId)
                .add("memberId", memberId)
                .add("openid", openid)
                .add("weixinId", weixinId)
                .add("status", status)
                .add("generationTime", generationTime.toString("yyyy-MM-dd HH:mm:ss"))
                .add("message", message)
                .add("payload", payload)
                .toString();
    }
}
