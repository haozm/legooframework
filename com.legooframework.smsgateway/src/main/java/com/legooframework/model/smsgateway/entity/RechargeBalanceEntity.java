package com.legooframework.model.smsgateway.entity;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RechargeBalanceEntity extends BaseEntity<String> {

    private final Integer companyId, storeId;
    private List<Integer> storeIds;
    private String groupName;
    private final RechargeScope rechargeScope;
    private long balance = 0L;

    // 创建虚拟分组
    RechargeBalanceEntity(OrgEntity company, List<StoEntity> stores, String groupName) {
        super(CommonsUtils.randomId(16).toUpperCase(), company.getId().longValue(), -1L);
        this.companyId = company.getId();
        this.storeId = null;
        if (CollectionUtils.isEmpty(stores)) {
            this.storeIds = null;
        } else {
            this.storeIds = stores.stream().mapToInt(BaseEntity::getId).boxed().collect(Collectors.toList());
            this.storeIds.sort(Comparator.naturalOrder());
        }
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
            if (this.rechargeScope == RechargeScope.StoreGroup && !Strings.isNullOrEmpty(res.getString("storeIds"))) {
                String storeIds_raw = res.getString("storeIds");
                this.storeIds = Stream.of(StringUtils.split(storeIds_raw, ','))
                        .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
                this.storeIds.sort(Comparator.naturalOrder());
            } else {
                this.storeIds = null;
            }
            this.balance = res.getLong("balance");
            this.groupName = res.getString("groupName");
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

    boolean deduction(int size) {
        Preconditions.checkState(size >= 0, "待扣除的短信数量需为正数，非法取值：%s", size);
        if (this.balance >= size) {
            this.balance = this.balance - size;
            return true;
        } else {
            return false;
        }
    }

    boolean addStores(List<StoEntity> stores) {
        Preconditions.checkState(RechargeScope.StoreGroup == this.rechargeScope);
        boolean addFlag = false;
        if (this.storeIds == null) this.storeIds = Lists.newArrayList();
        for (StoEntity $it : stores) {
            if (this.storeIds.contains($it.getId()))
                continue;
            this.storeIds.add($it.getId());
            addFlag = true;
        }
        this.storeIds.sort(Comparator.naturalOrder());
        return addFlag;
    }

    boolean delStores(List<StoEntity> stores) {
        if (isEmptyStoreIds()) return false;
        Preconditions.checkState(RechargeScope.StoreGroup == this.rechargeScope);
        boolean delFlag = false;
        Set<Integer> removeIds = stores.stream().mapToInt(BaseEntity::getId).boxed().collect(Collectors.toSet());
        Set<Integer> rawIds = Sets.newHashSet(this.storeIds);
        rawIds.removeAll(removeIds);
        delFlag = !SetUtils.isEqualSet(rawIds, this.storeIds);
        if (delFlag) {
            this.storeIds = CollectionUtils.isEmpty(rawIds) ? null : Lists.newArrayList(rawIds);
        }
        return delFlag;
    }

    public boolean isEmptyStoreIds() {
        return CollectionUtils.isEmpty(this.storeIds);
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

    String getStoreIdsRaw() {
        return CollectionUtils.isEmpty(this.storeIds) ? null : Joiner.on(',').join(this.storeIds);
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

    boolean isEmpty() {
        return balance == 0L;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("companyId", "storeId", "storeIds", "rechargeScope", "balance");
        params.put("companyId", companyId);
        params.put("storeId", storeId == null ? -1 : storeId);
        if (CollectionUtils.isNotEmpty(this.storeIds)) {
            params.put("storeIds", Joiner.on(',').join(this.storeIds));
        } else {
            params.put("storeIds", null);
        }
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
