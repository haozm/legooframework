package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class RechargeBalanceEntity extends BaseEntity<String> {

    private final Integer companyId, storeId;
    private final String storeGroupId;
    private final RechargeScope rechargeScope;
    private Long balance = 0L;
    private final Set<Integer> storeIds;

    RechargeBalanceEntity(RechargeDetailEntity recharge) {
        super(CommonsUtils.randomId(16).toUpperCase(), recharge.getTenantId(), -1L);
        this.companyId = recharge.getCompanyId();
        this.storeId = recharge.getStoreId();
        this.storeGroupId = recharge.getStoreGroupId();
        this.rechargeScope = recharge.getRechargeScope();
        this.storeIds = null;
    }

    RechargeBalanceEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = res.getInt("companyId");
            this.storeId = res.getInt("storeId");
            this.storeGroupId = res.getString("storeGroupId");
            this.rechargeScope = RechargeScope.paras(res.getInt("rechargeScope"));
            this.balance = res.getLong("balance");
            this.storeIds = Sets.newHashSet();
            String _storeIds = res.getString("storeIds");
            if (StringUtils.isNotEmpty(_storeIds)) {
                Stream.of(StringUtils.split(_storeIds, ',')).forEach(x -> this.storeIds.add(Integer.valueOf(x)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore RechargeBalanceEntity has SQLException", e);
        }
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

    long deduction(long size) {
        Preconditions.checkState(size > 0, "待扣除的短信数量需为正数，非法取值：%s", size);
        if (this.balance >= size) {
            this.balance = this.balance - size;
            return size;
        } else {
            long enbaled_size = this.balance;
            this.balance = 0L;
            return size - enbaled_size;
        }
    }

    RechargeScope getRechargeScope() {
        return rechargeScope;
    }

    public void addBalance(int smsNum) {
        this.balance += smsNum;
    }

    public Optional<Set<Integer>> getStoreIds() {
        Preconditions.checkState(RechargeScope.StoreGroup == rechargeScope, "非法的访问...%s", rechargeScope);
        return Optional.ofNullable(CollectionUtils.isEmpty(storeIds) ? null : storeIds);
    }

    boolean hasBlance() {
        return this.balance > 0;
    }

    boolean contains(CrmStoreEntity store) {
        boolean matched = false;
        switch (rechargeScope) {
            case Store:
                matched = this.companyId.equals(store.getCompanyId()) && this.storeId.equals(store.getId());
                break;
            case StoreGroup:
                matched = this.companyId.equals(store.getCompanyId()) && this.storeIds.contains(store.getId());
                break;
            case Company:
                matched = this.companyId.equals(store.getCompanyId());
                break;
            default:
                throw new RuntimeException(String.format("无发匹配门店%s 对应的规则", store));
        }
        return matched;
    }

    public Long getBalance() {
        return balance;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("companyId", "storeId", "storeIds",
                "storeGroupId", "rechargeScope", "balance");
        params.put("companyId", companyId);
        params.put("storeId", storeId == null ? -1 : storeId);
        params.put("storeGroupId", storeGroupId == null ? "NONE" : storeGroupId);
        params.put("rechargeScope", rechargeScope.getScope());
        params.put("balance", balance);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RechargeBalanceEntity)) return false;
        if (!super.equals(o)) return false;
        RechargeBalanceEntity that = (RechargeBalanceEntity) o;
        return Objects.equal(balance, that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), balance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("storeGroupId", storeGroupId)
                .add("rechargeScope", rechargeScope)
                .add("balance", balance)
                .add("storeIds", storeIds)
                .toString();
    }
}
