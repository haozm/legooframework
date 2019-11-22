package com.legooframework.model.smsgateway.entity;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 短信费用充值明细
 */
public class RechargeDetailEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final String storeIds;
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
            this.rechargeType = RechargeType.paras(res.getInt("rechargeType"));
            this.rechargeScope = RechargeScope.paras(res.getInt("rechargeScope"));
            if (rechargeScope == RechargeScope.StoreGroup) {
                this.storeIds = res.getString("storeIds");
            } else {
                this.storeIds = null;
            }
            this.amount = res.getLong("amount");
            this.totalQuantity = res.getInt("totalQuantity");
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
        ps.setObject(3, this.storeId == null ? -1 : storeId);
        ps.setObject(4, this.storeIds);
        ps.setObject(5, this.rechargeScope.getScope());
        ps.setObject(6, this.rechargeType.getType());
        ps.setObject(7, this.ruleId);
        ps.setObject(8, this.amount);
        ps.setObject(9, this.totalQuantity);
        ps.setObject(10, this.getTenantId());
        ps.setObject(11, this.getCreator());
    }

    private RechargeDetailEntity(Integer companyId, Integer storeId, String storeIds, RechargeScope rechargeScope,
                                 RechargeRuleEntity rechargeRule, ReimburseResDto reimburseDto, long rechargeAmount,
                                 RechargeType rechargeType,
                                 int totalQuantity, LoginContext user) {
        super(CommonsUtils.randomId(16), companyId.longValue(), user.getLoginId());
        Preconditions.checkArgument(rechargeAmount >= 0, "充值金额必须大于或者等于0");
        this.companyId = companyId;
        this.rechargeType = rechargeType;
        this.rechargeScope = rechargeScope;
        Preconditions.checkNotNull(rechargeScope, "参数 rechargeScope 不可以为空值...");
        switch (rechargeScope) {
            case Company:
                this.storeId = null;
                this.storeIds = null;
                break;
            case Store:
                this.storeId = storeId;
                this.storeIds = null;
                break;
            case StoreGroup:
                this.storeId = null;
                Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds));
                this.storeIds = storeIds;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的参数 rechargeScope =%s", rechargeScope));
        }
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
            case Reimburse:
                this.ruleId = reimburseDto.getBatchNo();
                this.totalQuantity = totalQuantity;
                this.amount = 0L;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的充值类型..%s", rechargeType));
        }
    }

    /**
     * 退款动作
     *
     * @param balanceEntity
     * @param smsNum
     * @return
     */
    public static RechargeDetailEntity writeOff(RechargeBalanceEntity balanceEntity, int smsNum) {
        RechargeDetailEntity res;
        switch (balanceEntity.getRechargeScope()) {
            case Store:
                res = new RechargeDetailEntity(balanceEntity.getCompanyId(), balanceEntity.getStoreId(), null,
                        RechargeScope.Store, null, null, 0, RechargeType.Reimburse, smsNum, LoginContextHolder.getAnonymousCtx());
                break;
            case Company:
                res = new RechargeDetailEntity(balanceEntity.getCompanyId(), -1, null,
                        RechargeScope.Company, null, null, 0, RechargeType.Reimburse, smsNum, LoginContextHolder.getAnonymousCtx());
                break;
            case StoreGroup:
                // TODO
//                res = new RechargeDetailEntity(balanceEntity.getCompanyId(), -1, balanceEntity.getStoreIds(),
//                        RechargeScope.StoreGroup, null, 0, RechargeType.Reimburse, smsNum, LoginContextHolder.getAnonymousCtx());
                break;
            default:
                throw new RuntimeException("异常的充值范围...");
        }
        return null;
    }

    static RechargeDetailEntity rechargeByStore(StoEntity store, RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(store.getCompanyId(), store.getId(), null,
                RechargeScope.Store, rechargeRule, null, rechargeAmount, RechargeType.Recharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity prechargeByStore(StoEntity store, RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(store.getCompanyId(), store.getId(), null,
                RechargeScope.Store, rechargeRule, null, rechargeAmount, RechargeType.Precharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity freechargeByStore(StoEntity store, int totalQuantity) {
        return new RechargeDetailEntity(store.getCompanyId(), store.getId(), null,
                RechargeScope.Store, null, null, 0L, RechargeType.FreeCharge, totalQuantity,
                LoginContextHolder.get());
    }

    static RechargeDetailEntity rechargeByStoreGroup(OrgEntity company, String storeIds,
                                                     RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, storeIds, RechargeScope.StoreGroup,
                rechargeRule, null, rechargeAmount, RechargeType.Recharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity prechargeByStoreGroup(OrgEntity company, String storeIds,
                                                      RechargeRuleEntity rechargeRule, long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, storeIds, RechargeScope.StoreGroup,
                rechargeRule, null, rechargeAmount, RechargeType.Precharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity freechargeByStoreGroup(OrgEntity company, String storeIds, int totalQuantity) {
        return new RechargeDetailEntity(company.getId(), -1, storeIds, RechargeScope.StoreGroup,
                null, null, 0L, RechargeType.FreeCharge, totalQuantity, LoginContextHolder.get());
    }

    static RechargeDetailEntity rechargeByCompany(OrgEntity company, RechargeRuleEntity rechargeRule,
                                                  long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, null, RechargeScope.Company, rechargeRule,
                null, rechargeAmount, RechargeType.Recharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity prechargeByCompany(OrgEntity company, RechargeRuleEntity rechargeRule,
                                                   long rechargeAmount) {
        return new RechargeDetailEntity(company.getId(), -1, null, RechargeScope.Company, rechargeRule, null,
                rechargeAmount, RechargeType.Precharge, 0, LoginContextHolder.get());
    }

    static RechargeDetailEntity freechargeByCompany(OrgEntity company, int totalQuantity) {
        return new RechargeDetailEntity(company.getId(), -1, null, RechargeScope.Company, null, null,
                0L, RechargeType.FreeCharge, totalQuantity, LoginContextHolder.get());
    }

    static RechargeDetailEntity reimburseByCompany(ReimburseResDto reimburseDto) {
        return new RechargeDetailEntity(reimburseDto.getCompanyId(), -1, null, RechargeScope.Company, null, reimburseDto,
                0L, RechargeType.Reimburse, reimburseDto.getTotalSmsCount(), LoginContextHolder.get());
    }

    static RechargeDetailEntity reimburseByStoreGroup(RechargeBalanceEntity balance, ReimburseResDto reimburseDto) {
        return new RechargeDetailEntity(reimburseDto.getCompanyId(), -1, balance.getId(), RechargeScope.StoreGroup, null,
                reimburseDto, 0L, RechargeType.Reimburse, reimburseDto.getTotalSmsCount(), LoginContextHolder.get());
    }

    static RechargeDetailEntity reimburseByStore(ReimburseResDto reimburseDto) {
        return new RechargeDetailEntity(reimburseDto.getCompanyId(), reimburseDto.getStoreId(), null,
                RechargeScope.Store, null, reimburseDto,
                0L, RechargeType.Reimburse, reimburseDto.getTotalSmsCount(), LoginContextHolder.get());
    }

    private RechargeDetailEntity(RechargeDetailEntity recharge, LoginContext user) {
        super(CommonsUtils.randomId(16), recharge.companyId.longValue(), user.getLoginId());
        this.companyId = recharge.companyId;
        this.rechargeType = RechargeType.Deduction;
        this.storeId = recharge.storeId;
        this.storeIds = recharge.storeIds;
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

    boolean isStoreGroup() {
        return RechargeScope.StoreGroup == this.rechargeScope;
    }

    String getStoreIds() {
        return storeIds;
    }

    int getTotalQuantity() {
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
        params.put("storeId", storeId == null ? -1 : storeId);
        params.put("storeIds", storeIds);
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
                Objects.equal(storeIds, that.storeIds) &&
                Objects.equal(storeId, that.storeId) &&
                rechargeScope == that.rechargeScope &&
                rechargeType == that.rechargeType &&
                Objects.equal(ruleId, that.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, storeId, storeIds, rechargeScope, rechargeType,
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
                .add("storeIds", storeIds)
                .add("rechargeScope", rechargeScope)
                .add("rechargeType", rechargeType)
                .add("ruleId", ruleId)
                .add("amount", amount)
                .add("totalQuantity", totalQuantity)
                .add("usedQuantity", usedQuantity)
                .toString();
    }
}
