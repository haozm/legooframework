package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * 短信充值规则
 */
public class RechargeRuleEntity extends BaseEntity<String> {

    private Range<Long> range;
    private BigDecimal unitPrice;
    private boolean enabled, temporary;
    private Integer companyId;
    private String remarks;
    private LocalDate expiredDate;

    RechargeRuleEntity(Long min, Long max, double unitPrice, CrmOrganizationEntity company, String remarks,
                       boolean temporary, LocalDate expiredDate) {
        this(min, max, unitPrice, company == null ? null : company.getId(), temporary, remarks, expiredDate);
    }

    private RechargeRuleEntity(Long min, Long max, double unitPrice, Integer companyId, boolean temporary,
                               String remarks, LocalDate expiredDate) {
        super(CommonsUtils.randomId(16), 1000000L, -1L);
        if (min == null) min = 0L;
        if (max == null) max = 999999999900L;
        Preconditions.checkArgument(min > 0 && max > min, "(%s,%s) 取值异常....", min, max);
        this.range = Range.closed(min, max);
        Preconditions.checkArgument(unitPrice > 0, "短信单价 %s 取值错误...", unitPrice);
        this.unitPrice = new BigDecimal(unitPrice);
        this.enabled = true;
        this.temporary = temporary;
        this.remarks = remarks;
        this.expiredDate = expiredDate;
        this.companyId = companyId == null ? -1 : companyId;
    }

    RechargeRuleEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.unitPrice = res.getBigDecimal("unitPrice");
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.enabled = ResultSetUtil.getObject(res, "enabled", Integer.class) == 1;
            this.range = ResultSetUtil.getLongRange(res, "amountRange");
            this.remarks = ResultSetUtil.getOptString(res, "remarks", null);
            this.expiredDate = ResultSetUtil.getLocalDate(res, "expiredDate");
            this.temporary = ResultSetUtil.getOptObject(res, "temporary", Integer.class).orElse(0) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Restore RechargeRuleEntity has SQLException", e);
        }
    }

    double getUnitPrice() {
        return unitPrice.doubleValue();
    }

    Optional<RechargeRuleEntity> disabled() {
        if (!this.enabled) return Optional.empty();
        RechargeRuleEntity clone = (RechargeRuleEntity) cloneMe();
        clone.enabled = false;
        return Optional.of(clone);
    }

    Optional<RechargeRuleEntity> enabled() {
        if (this.enabled) return Optional.empty();
        RechargeRuleEntity clone = (RechargeRuleEntity) cloneMe();
        clone.enabled = true;
        return Optional.of(clone);
    }

    int totalQuantity(long rechargeAmount) {
        BigDecimal one = new BigDecimal(String.valueOf(rechargeAmount));
        return one.divide(unitPrice, BigDecimal.ROUND_HALF_UP).intValue();
    }

    private boolean isTemporary() {
        return temporary;
    }

    public boolean isEnabled() {
        return enabled;
    }

    boolean isSuitable(CrmOrganizationEntity company, long rechargeAmount) {
        return enabled && isOwnerCompany(company) && this.range.contains(rechargeAmount)
                && !isTemporary() && isNotExpired();
    }

    public RechargeRuleEntity(String id) {
        super(id);
    }

    public boolean isOwnerCompany(CrmOrganizationEntity company) {
        if (this.companyId == -1) return true;
        return this.companyId.equals(company.getId());
    }

    public boolean isGlobalRule() {
        return this.companyId == -1;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("range", String.format("%s,%s", range.lowerEndpoint() / 100, range.upperEndpoint() / 100));
        params.put("unitPrice", unitPrice.doubleValue());
        params.put("companyName", isGlobalRule() ? "全局" : companyId);
        params.put("companyId", companyId);
        params.put("remarks", remarks);
        params.put("enabled", enabled);
        params.put("expiredDate", expiredDate == null ? "永久有效" : expiredDate.toString("yyyy-MM-dd"));
        return params;
    }

    // 是否过期
    public boolean isNotExpired() {
        return expiredDate == null || LocalDate.now().isBefore(expiredDate);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("range", "enbaled", "temporary");
        params.put("enabled", enabled ? 1 : 0);
        params.put("amountRange", range.toString());
        params.put("unitPrice", unitPrice);
        params.put("companyId", companyId);
        params.put("remarks", remarks);
        params.put("expiredDate", expiredDate == null ? null : expiredDate.toDate());
        params.put("temporary", temporary ? 1 : 0);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RechargeRuleEntity)) return false;
        RechargeRuleEntity that = (RechargeRuleEntity) o;
        return unitPrice.equals(that.unitPrice) &&
                enabled == that.enabled &&
                Objects.equal(range, that.range) &&
                Objects.equal(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(range, unitPrice, enabled, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("range", range)
                .add("unitPrice", unitPrice)
                .add("enabled", enabled)
                .add("companyId", companyId)
                .add("expiredDate", expiredDate)
                .add("temporary", temporary)
                .toString();
    }
}
