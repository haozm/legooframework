package com.legooframework.model.smsgateway.entity;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RechargeBalanceEntity extends BaseEntity<String> {

    private final Integer companyId, storeId;
    private final List<Integer> storeIds;
    private String groupName;
    private final RechargeScope rechargeScope;
    private Long balance = 0L;

    // 创建虚拟分组
    RechargeBalanceEntity(OrgEntity company, List<StoEntity> stores, String groupName) {
        super(CommonsUtils.randomId(16).toUpperCase(), company.getId().longValue(), -1L);
        this.companyId = company.getId();
        this.storeId = null;
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(stores));
        this.storeIds = stores.stream().mapToInt(BaseEntity::getId).boxed().collect(Collectors.toList());
        this.storeIds.sort(Comparator.naturalOrder());
        this.groupName = groupName;
        this.rechargeScope = RechargeScope.StoreGroup;
        this.balance = 0L;
    }

    // 充值创建分组
    RechargeBalanceEntity(RechargeDetailEntity recharge) {
        super(recharge.isStoreGroup() ? recharge.getStoreIds() : CommonsUtils.randomId(16).toUpperCase(),
                recharge.getTenantId(), -1L);
        this.companyId = recharge.getCompanyId();
        this.storeId = recharge.getStoreId();
        this.rechargeScope = recharge.getRechargeScope();
        this.storeIds = null;
    }

    String getGroupName() {
        return groupName;
    }

    RechargeBalanceEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = res.getInt("companyId");
            this.storeId = res.getInt("storeId");
            this.rechargeScope = RechargeScope.paras(res.getInt("rechargeScope"));
            if (this.rechargeScope == RechargeScope.StoreGroup) {
                String storeIds_raw = res.getString("storeIds");
                Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds_raw), "数据异常 storeIds 值为空...");
                this.storeIds = ImmutableList.copyOf(Stream.of(StringUtils.split(storeIds_raw, ',')).mapToInt(Integer::parseInt).boxed()
                        .collect(Collectors.toList()));
            } else {
                this.storeIds = null;
            }
            this.balance = res.getLong("balance");
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

    public List<Integer> getStoreIds() {
        return storeIds;
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

    boolean hasBlance() {
        return this.balance > 0;
    }

    boolean contains(StoEntity store) {
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

    long getBalance() {
        return balance;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("companyId", "storeId", "storeIds",
                "storeGroupId", "rechargeScope", "balance");
        params.put("companyId", companyId);
        params.put("storeId", storeId == null ? -1 : storeId);
        params.put("storeIds", CollectionUtils.isEmpty(this.storeIds) ? null : Joiner.on(',').join(this.storeIds));
        params.put("rechargeScope", rechargeScope.getScope());
        params.put("balance", balance);
        params.put("groupName", groupName);
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
                .add("storeIds", storeIds)
                .add("rechargeScope", rechargeScope)
                .add("balance", balance)
                .add("storeIds", storeIds)
                .toString();
    }
}
