package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class SaleRecordEntity extends BaseEntity<Integer> implements Comparator<SaleRecordEntity> {

    private final String oldSaleRecordId, saleOrderNo;
    private final BigDecimal saleTotalAmount;
    private final int status, goodCount;
    private final LocalDateTime saleDate, modifyDate;
    private final Integer companyId, storeId, memberId, shoppingguideId;
    private final boolean sample;
    private String changeFlag;
    private final List<SaleRecordDetailEntity> recordDetails;

    @Override
    public int compare(SaleRecordEntity o1, SaleRecordEntity o2) {
        return o1.saleDate.equals(o2.saleDate) ? 0 : o1.saleDate.isBefore(o2.saleDate) ? 1 : -1;
    }

    SaleRecordEntity(Integer id, ResultSet res, Long tenantId, Long creator, boolean sample) {
        super(id, tenantId, creator);
        try {
            this.oldSaleRecordId = ResultSetUtil.getOptString(res, "oldSaleRecordId", null);
            this.saleOrderNo = ResultSetUtil.getOptString(res, "saleOrderNo", null);
            this.status = ResultSetUtil.getObject(res, "status", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.memberId = ResultSetUtil.getObject(res, "memberId", Integer.class);
            this.changeFlag = ResultSetUtil.getOptString(res, "changeFlag", null);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.saleDate = LocalDateTime.fromDateFields(res.getTimestamp("createTime"));
            this.modifyDate = LocalDateTime.fromDateFields(res.getTimestamp("modifyDate"));
            this.saleTotalAmount = res.getBigDecimal("saleTotalAmount") == null ? new BigDecimal(0.0) :
                    res.getBigDecimal("saleTotalAmount");
            this.goodCount = res.getInt("goodsCount");
            this.shoppingguideId = ResultSetUtil.getOptObject(res, "shoppingguideId", Integer.class).orElse(null);
            this.sample = sample;
            if (!this.sample) {
                String _details = ResultSetUtil.getOptString(res, "details", null);
                if (!Strings.isNullOrEmpty(_details)) {
                    this.recordDetails = Lists.newArrayList();
                    Stream.of(StringUtils.split(_details, '@')).map(s -> StringUtils.split(s, '$')).forEach(args -> {
                        this.recordDetails.add(new SaleRecordDetailEntity(Integer.valueOf(args[0]),
                                Double.valueOf(args[1]), Double.valueOf(args[2]),
                                Integer.valueOf(args[3]), Integer.valueOf(args[4]),
                                Integer.valueOf(args[5]), Integer.valueOf(args[6]), args[7], args[8], Integer.valueOf(args[9])));
                    });
                } else {
                    this.recordDetails = null;
                }
            } else {
                this.recordDetails = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore SaleRecordEntity has SQLException", e);
        }
    }

    public List<SaleRecordDetailEntity> getRecordDetails() {
        Preconditions.checkState(!sample, "当前模式为’sample‘ 模式，无法加载其详情...");
        return recordDetails;
    }

    String getChangeFlag() {
        return changeFlag;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Optional<Integer> getShoppingguideId() {
        return Optional.ofNullable(shoppingguideId);
    }

    public Integer getMemberId() {
        return memberId;
    }

    public LocalDateTime getModifyDate() {
        return modifyDate;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public boolean isModified() {
        String _temp = String.format("%s%s%s%.2f%s%s", companyId, storeId, memberId == null ? 0 : memberId,
                saleTotalAmount, saleDate.toString("yyyy-MM-dd HH:mm:ss"), status);
        return null != changeFlag && !StringUtils.equals(this.changeFlag, _temp);
    }

    public boolean isNeedUpdateChangeFlag() {
        return null == changeFlag || isModified();
    }

    public BigDecimal getSaleTotalAmount() {
        return saleTotalAmount;
    }

    public boolean isNegativeOrZore() {
        return saleTotalAmount.longValue() <= 0;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("companyId", companyId);
        params.put("memberId", memberId);
        params.put("saleDate", saleDate.toString("yyyy-MM-dd HH:mm:ss"));
        params.put("saleCount", goodCount);
        params.put("saleOrderNo", saleOrderNo);
        params.put("changeFlag", changeFlag);
        params.put("saleTotalAmount", saleTotalAmount);
        if (CollectionUtils.isNotEmpty(recordDetails)) {
            List<Map<String, Object>> detail = Lists.newArrayList();
            for (SaleRecordDetailEntity em : recordDetails) {
                detail.add(em.toViewMap());
            }
            params.put("saleRecordDetails", detail);
        } else {
            params.put("saleRecordDetails", null);
        }
        return params;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SaleRecordEntity that = (SaleRecordEntity) o;
        return status == that.status &&
                Objects.equals(oldSaleRecordId, that.oldSaleRecordId) &&
                Objects.equals(saleOrderNo, that.saleOrderNo) &&
                Objects.equals(saleTotalAmount, that.saleTotalAmount) &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), oldSaleRecordId, saleOrderNo, saleTotalAmount, status,
                companyId, storeId, memberId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("memberId", memberId)
                .add("saleTotalAmount", saleTotalAmount)
                .add("saleDate", saleDate)
                .add("shoppingguideId", shoppingguideId)
                .add("saleOrderNo", saleOrderNo)
                .toString();
    }
}
