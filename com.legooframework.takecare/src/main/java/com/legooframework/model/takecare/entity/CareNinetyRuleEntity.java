package com.legooframework.model.takecare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class CareNinetyRuleEntity extends BaseEntity<Integer> implements BatchSetter {

    private Integer companyId, storeId;
    private boolean enabled;
    private int toHour, toNode1, toNode3, toNode7, toNode15, toNode30, toNode60, toNode90, limitDays;
    private LocalDateTime createTime;
    private String remark;
    private BigDecimal minAmount, limitAmount;

    private CareNinetyRuleEntity(Integer companyId, Integer storeId, boolean enabled, int toHour, int toNode1,
                                 int toNode3, int toNode7, int toNode15, int toNode30, int toNode60, int toNode90,
                                 String remark, int limitDays, BigDecimal minAmount, BigDecimal limitAmount) {
        super(0);
        this.companyId = companyId;
        this.storeId = storeId;
        this.enabled = enabled;
        Preconditions.checkArgument(toHour >= 0 && toHour < 24, "小时跨度需小于24小时....");
        this.toHour = toHour;
        this.toNode1 = toNode1;
        if (toNode3 > 0)
            Preconditions.checkArgument(Ints.max(toNode3, toNode1) == toNode3, "第2节点异常");
        this.toNode3 = toNode3;
        if (toNode7 > 0)
            Preconditions.checkArgument(Ints.max(toNode3, toNode7, toNode1) == toNode7, "第3节点异常");
        this.toNode7 = toNode7;
        if (toNode15 > 0)
            Preconditions.checkArgument(Ints.max(toNode3, toNode7, toNode15, toNode1) == toNode15, "第4节点异常");
        this.toNode15 = toNode15;
        if (toNode30 > 0)
            Preconditions.checkArgument(Ints.max(toNode3, toNode7, toNode30, toNode15, toNode1) == toNode30, "第5节点异常");
        this.toNode30 = toNode30;
        if (toNode60 > 0)
            Preconditions.checkArgument(Ints.max(toNode3, toNode60, toNode7, toNode30, toNode15, toNode1) == toNode60, "第6节点异常");
        this.toNode60 = toNode60;
        if (toNode90 > 0)
            Preconditions.checkArgument(Ints.max(toNode3, toNode60, toNode90, toNode7, toNode30, toNode15, toNode1) == toNode90, "第7节点异常");
        this.toNode90 = toNode90;
        this.createTime = LocalDateTime.now();
        this.remark = remark;
        Preconditions.checkArgument(limitDays >= 0);
        this.limitDays = limitDays;
        if (this.limitDays > 0) {
            Preconditions.checkArgument(limitAmount != null && limitAmount.doubleValue() > 0.0D);
            this.limitAmount = limitAmount;
        } else {
            this.limitAmount = new BigDecimal(0.0D);
        }
        this.minAmount = minAmount == null ? new BigDecimal(0.0D) : minAmount;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // store_id, to_H, to_1, to_3, to_7, to_15, to_30, to_60, to_90, createtime,
        ps.setObject(1, storeId);
        ps.setObject(2, toHour);
        ps.setObject(3, toNode1);
        ps.setObject(4, toNode3);
        ps.setObject(5, toNode7);
        ps.setObject(6, toNode15);
        ps.setObject(7, toNode30);
        ps.setObject(8, toNode60);
        ps.setObject(9, toNode90);
        // remark, enable, company_id, min_amount, limit_days, limit_amount
        ps.setObject(10, remark);
        ps.setObject(11, enabled ? 1 : 0);
        ps.setObject(12, companyId);
        ps.setObject(13, minAmount);
        ps.setObject(14, limitDays);
        ps.setObject(15, limitAmount);
    }

    static CareNinetyRuleEntity createByCompany(OrgEntity company, int toHour, int toNode1,
                                                int toNode3, int toNode7, int toNode15, int toNode30, int toNode60, int toNode90,
                                                String remark, int limitDays, double minAmount, double limitAmount) {
        return new CareNinetyRuleEntity(company.getId(), 0, true, toHour, toNode1, toNode3, toNode7, toNode15,
                toNode30, toNode60, toNode90, remark, limitDays, new BigDecimal(minAmount), new BigDecimal(limitAmount));
    }

    static CareNinetyRuleEntity createByStore(StoEntity store, int toHour, int toNode1,
                                              int toNode3, int toNode7, int toNode15, int toNode30, int toNode60, int toNode90,
                                              String remark, int limitDays, double minAmount, double limitAmount) {
        return new CareNinetyRuleEntity(store.getCompanyId(), store.getId(), true, toHour, toNode1, toNode3, toNode7, toNode15,
                toNode30, toNode60, toNode90, remark, limitDays, new BigDecimal(minAmount), new BigDecimal(limitAmount));
    }

    Integer getCompanyId() {
        return companyId;
    }

    Integer getStoreId() {
        return storeId == null ? 0 : storeId;
    }

    boolean isEnabled() {
        return enabled;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", this.companyId);
        params.put("storeId", this.storeId == null ? 0 : storeId);
        return params;
    }

    CareNinetyRuleEntity(ResultSet resultSet) throws RuntimeException {
        super(0);
        try {
            this.companyId = resultSet.getInt("company_id");
            this.storeId = resultSet.getInt("store_id");
            this.enabled = resultSet.getInt("enable") == 1;
            this.toHour = resultSet.getInt("to_H");
            this.toNode1 = resultSet.getInt("to_1");
            this.toNode3 = resultSet.getInt("to_3");
            this.toNode7 = resultSet.getInt("to_7");
            this.toNode15 = resultSet.getInt("to_15");
            this.toNode30 = resultSet.getInt("to_30");
            this.toNode60 = resultSet.getInt("to_60");
            this.toNode90 = resultSet.getInt("to_90");
            this.createTime = LocalDateTime.fromDateFields(resultSet.getTimestamp("createtime"));
            this.remark = resultSet.getString("remark");
            this.limitDays = resultSet.getInt("limit_days");
            this.minAmount = resultSet.getBigDecimal("min_amount") == null ? new BigDecimal(0.0D) :
                    resultSet.getBigDecimal("min_amount");
            this.limitAmount = resultSet.getBigDecimal("limit_amount") == null ? new BigDecimal(0.0D) :
                    resultSet.getBigDecimal("limit_amount");
        } catch (SQLException e) {
            throw new RuntimeException("还原对象 CareNinetyRuleEntity 发生异常", e);
        }
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("toHour", toHour);
        params.put("toNode1", toNode1);
        params.put("toNode3", toNode3);
        params.put("toNode7", toNode7);
        params.put("toNode15", toNode15);
        params.put("toNode30", toNode30);
        params.put("toNode60", toNode60);
        params.put("toNode90", toNode90);
        params.put("limitDays", limitDays);
        params.put("enabled", enabled);
        params.put("remark", remark);
        params.put("createTime", createTime.toString("yyyy-MM-dd HH:mm:ss"));
        params.put("limitAmount", limitAmount.doubleValue());
        params.put("minAmount", minAmount.doubleValue());
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CareNinetyRuleEntity that = (CareNinetyRuleEntity) o;
        return toHour == that.toHour &&
                toNode1 == that.toNode1 &&
                toNode3 == that.toNode3 &&
                toNode7 == that.toNode7 &&
                toNode15 == that.toNode15 &&
                toNode30 == that.toNode30 &&
                toNode60 == that.toNode60 &&
                toNode90 == that.toNode90 &&
                limitDays == that.limitDays &&
                companyId.equals(that.companyId) &&
                storeId.equals(that.storeId) &&
                minAmount.equals(that.minAmount) &&
                limitAmount.equals(that.limitAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), companyId, storeId, toHour, toNode1, toNode3, toNode7, toNode15,
                toNode30, toNode60, toNode90, limitDays, minAmount, limitAmount);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("companyId", companyId).add("storeId", storeId)
                .add("enabled", enabled).add("toHour", toHour).add("toNode1", toNode1)
                .add("toNode3", toNode3).add("toNode7", toNode7).add("toNode15", toNode15)
                .add("toNode30", toNode30).add("toNode60", toNode60).add("toNode90", toNode90)
                .add("limitDays", limitDays).add("createTime", createTime).add("remark", remark)
                .add("minAmount", minAmount).add("limitAmount", limitAmount)
                .toString();
    }
}
