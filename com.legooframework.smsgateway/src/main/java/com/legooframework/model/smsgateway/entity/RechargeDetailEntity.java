package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 短信费用充值明细
 */
public class RechargeDetailEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final String storeGroupId;
    private final RechargeScope rechargeScope;
    private final String ruleId;
    private final long amount;
    private final int totalQuantity;
    private final RechargeType rechargeType;
    private int usedQuantity;

    RechargeDetailEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = res.getInt("companyId");
            this.storeId = res.getInt("storeId");
            this.storeGroupId = res.getString("storeGroupId");
            this.amount = res.getLong("amount");
            this.rechargeType = RechargeType.paras(res.getInt("rechargeType"));
            this.totalQuantity = res.getInt("totalQuantity");
            this.rechargeScope = RechargeScope.paras(res.getInt("rechargeScope"));
            this.ruleId = res.getString("ruleId");
            this.usedQuantity = res.getInt("usedQuantity");
        } catch (SQLException e) {
            throw new RuntimeException("Restore RechargeDetailEntity has SQLException", e);
        }
    }

    RechargeScope getRechargeScope() {
        return rechargeScope;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        (id, company_id, store_id, store_group_id, recharge_scope, recharge_type, rule_id,
//                recharge_amount, total_quantity,  tenant_id, creator, createTime)
        ps.setObject(1, this.getId());
        ps.setObject(2, this.companyId);
        ps.setObject(3, this.storeId);
        ps.setObject(4, this.storeGroupId);
        ps.setObject(5, this.rechargeScope.getScope());
        ps.setObject(6, this.rechargeType.getType());
        ps.setObject(7, this.ruleId);
        ps.setObject(8, this.amount);
        ps.setObject(9, this.totalQuantity);
        ps.setObject(10, this.getTenantId());
        ps.setObject(11, this.getCreator());
        ps.setObject(12, this.getCreateTime().toDate());
    }

    private RechargeDetailEntity(Integer companyId, Integer storeId, String storeGroupId, RechargeScope rechargeScope,
                                 RechargeRuleEntity rechargeRule, long rechargeAmount, RechargeType rechargeType,
                                 int totalQuantity, LoginContext user) {
        super(CommonsUtils.randomId(16).toUpperCase(), companyId.longValue(), user.getLoginId());
        Preconditions.checkArgument(rechargeAmount >= 0, "充值金额必须大于或者等于0");
        this.companyId = companyId;
        this.rechargeType = rechargeType;
        this.storeId = storeId;
        this.storeGroupId = storeGroupId;
        this.rechargeScope = rechargeScope;
        switch (rechargeType) {
            case Recharge:
                this.ruleId = rechargeRule.getId();
                this.amount = rechargeAmount;
                this.totalQuantity = rechargeRule.totalQuantity(rechargeAmount);
                break;
            case Precharge:
                this.ruleId = rechargeRule.getId();
                this.amount = rechargeAmount;
                this.totalQuantity = rechargeRule.totalQuantity(rechargeAmount);
                break;
            case FreeCharge:
                this.ruleId = "free";
                this.totalQuantity = totalQuantity;
                this.amount = 0L;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的充值类型..%s", rechargeType));
        }
    }

    static RechargeDetailEntity rechargeByStore(CrmStoreEntity store, RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(store.getCompanyId(), store.getId(), null,
                RechargeScope.Store, rechargeRule, rechargeAmount, RechargeType.Recharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity prechargeByStore(CrmStoreEntity store, RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(store.getCompanyId(), store.getId(), null,
                RechargeScope.Store, rechargeRule, rechargeAmount, RechargeType.Precharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity freechargeByStore(CrmStoreEntity store, int totalQuantity) {
        return new RechargeDetailEntity(store.getCompanyId(), store.getId(), null,
                RechargeScope.Store, null, 0L, RechargeType.FreeCharge, totalQuantity,
                LoginContextHolder.get());
    }

    static RechargeDetailEntity rechargeByStoreGroup(CrmOrganizationEntity company, String storeGroupId,
                                                     RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, storeGroupId, RechargeScope.StoreGroup,
                rechargeRule, rechargeAmount, RechargeType.Recharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity prechargeByStoreGroup(CrmOrganizationEntity company, String storeGroupId,
                                                      RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, storeGroupId, RechargeScope.StoreGroup,
                rechargeRule, rechargeAmount, RechargeType.Precharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity freechargeByStoreGroup(CrmOrganizationEntity company, String storeGroupId, int totalQuantity) {
        return new RechargeDetailEntity(company.getId(), -1, storeGroupId, RechargeScope.StoreGroup,
                null, 0L, RechargeType.FreeCharge, totalQuantity, LoginContextHolder.get());
    }

    static RechargeDetailEntity rechargeByCompany(CrmOrganizationEntity company, RechargeRuleEntity rechargeRule,
                                                  long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, null, RechargeScope.Company, rechargeRule,
                rechargeAmount, RechargeType.Recharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity prechargeByCompany(CrmOrganizationEntity company, RechargeRuleEntity rechargeRule,
                                                   long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, null, RechargeScope.Company, rechargeRule,
                rechargeAmount, RechargeType.Precharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity freechargeByCompany(CrmOrganizationEntity company, int totalQuantity) {
        return new RechargeDetailEntity(company.getId(), -1, null, RechargeScope.Company, null,
                0L, RechargeType.FreeCharge, totalQuantity, LoginContextHolder.get());
    }

    private RechargeDetailEntity(RechargeDetailEntity recharge, LoginContext user) {
        super(CommonsUtils.randomId(16), recharge.companyId.longValue(), user.getLoginId());
        this.companyId = recharge.companyId;
        this.rechargeType = RechargeType.Deduction;
        this.storeId = recharge.storeId;
        this.storeGroupId = recharge.storeGroupId;
        this.rechargeScope = recharge.rechargeScope;
        this.ruleId = recharge.ruleId;
        this.totalQuantity = 0 - recharge.totalQuantity;
        this.amount = 0 - recharge.amount;
    }

    RechargeDetailEntity deduction(LoginContext user) {
        Preconditions.checkState(RechargeType.Precharge == this.rechargeType);
        return new RechargeDetailEntity(this, user);
    }

    Integer getCompanyId() {
        return companyId;
    }

    Integer getStoreId() {
        return storeId;
    }

    String getStoreGroupId() {
        return storeGroupId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("storeInclude", "storeExclude", "rechargeScope");
        params.put("rechargeScope", rechargeScope.getScope());
        params.put("rechargeType", rechargeType.getType());
        params.put("ruleId", ruleId);
        params.put("amount", amount);
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("storeGroupId", storeGroupId);
        params.put("totalQuantity", totalQuantity);
        params.put("usedQuantity", usedQuantity);
        return params;
    }

    int getUsedQuantity() {
        return usedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RechargeDetailEntity)) return false;
        if (!super.equals(o)) return false;
        RechargeDetailEntity that = (RechargeDetailEntity) o;
        return amount == that.amount &&
                totalQuantity == that.totalQuantity &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeGroupId, that.storeGroupId) &&
                Objects.equal(storeId, that.storeId) &&
                rechargeScope == that.rechargeScope &&
                rechargeType == that.rechargeType &&
                Objects.equal(ruleId, that.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, storeId, storeGroupId, rechargeScope, rechargeType,
                ruleId, amount, totalQuantity);
    }

    public int getBalanceQuantity() {
        return this.totalQuantity - this.usedQuantity;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("storeGroupId", storeGroupId)
                .add("rechargeScope", rechargeScope)
                .add("rechargeType", rechargeType)
                .add("ruleId", ruleId)
                .add("amount", amount)
                .add("totalQuantity", totalQuantity)
                .add("usedQuantity", usedQuantity)
                .toString();
    }
}
