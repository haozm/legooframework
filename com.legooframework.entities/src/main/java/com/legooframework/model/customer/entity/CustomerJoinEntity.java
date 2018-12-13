package com.legooframework.model.customer.entity;

import com.google.common.base.MoreObjects;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerJoinEntity extends BaseEntity<Long> implements BatchSetter {

    private Long memberId, publicId, weixinId;
    private final Long storeId;
    private Channel accountType;
    private CustomerId customerId;

    private CustomerJoinEntity(Long id, Long publicId, Long memberId, Long weixinId, Long storeId, Channel accountType) {
        super(id);
        this.publicId = publicId;
        this.memberId = memberId;
        this.weixinId = weixinId;
        this.storeId = storeId;
        this.customerId = new CustomerId(id, accountType, storeId);
        this.accountType = accountType;
    }

    CustomerJoinEntity(CustomerId customerId, LoginContext user) {
        super(customerId.getId(), user.getTenantId(), user.getLoginId());
        this.publicId = null;
        this.memberId = null;
        this.weixinId = null;
        this.storeId = customerId.getStoreId();
        this.accountType = customerId.getChannel();
        this.customerId = customerId;
    }

    CustomerJoinEntity(CustomerId customerId,Long weixinId,Long tenantId,LoginContext user){
    	  super(customerId.getId(),tenantId, user.getLoginId());
          this.weixinId = weixinId;
          this.storeId = customerId.getStoreId();
          this.accountType = customerId.getChannel();
          this.customerId = customerId;
    }
    
    public CustomerId getCustomerId() {
        return customerId;
    }

    private CustomerJoinEntity(Long id, Long tenantId, Long publicId, Long memberId, Long weixinId, Long storeId,
                               Channel accountType) {
        super(id, tenantId, null);
        this.publicId = publicId;
        this.memberId = memberId;
        this.weixinId = weixinId;
        this.storeId = storeId;
        this.customerId = new CustomerId(id, accountType, storeId);
        this.accountType = accountType;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        REPLACE INTO customer_join_info
//                ( id,weixin_id, member_id, public_id, account_type, store_id, tenant_id,  creator, createTime )
//        VALUES ( ? ,        ?,         ?,         ?,             ?,       ?,         ?,        ?, NOW())
        ps.setObject(1, this.getId());
        ps.setObject(2, this.getWeixinId().orElse(null));
        ps.setObject(3, this.getMemberId().orElse(null));
        ps.setObject(4, this.getPublicId().orElse(null));
        ps.setObject(5, this.accountType.getVal());
        ps.setObject(6, this.getStoreId());
        ps.setObject(7, this.getTenantId());
        ps.setObject(8, this.getCreator() == null ? -1L : this.getCreator());
    }

    CustomerJoinEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.publicId = ResultSetUtil.getOptObject(res, "publicId", Long.class).orElse(null);
            this.memberId = ResultSetUtil.getOptObject(res, "memberId", Long.class).orElse(null);
            this.weixinId = ResultSetUtil.getOptObject(res, "weixinId", Long.class).orElse(null);
            this.storeId = res.getLong("storeId");
            this.accountType = Channel.valueOf(res.getInt("accountType"));
            this.customerId = new CustomerId(id, accountType, storeId);
        } catch (SQLException e) {
            throw new RuntimeException("Restore CustomerJoinEntity's orgType has SQLException", e);
        }
    }

    public boolean isMerged(CustomerJoinEntity other) {
        Preconditions.checkNotNull(other);
        if (other.isMember()) return this.memberId != null && this.memberId.equals(other.getId());
        if (other.isWechat()) return this.weixinId != null && this.weixinId.equals(other.getId());
        return false;
    }


    public boolean isSameChannle(CustomerJoinEntity that) {
        return this.accountType == that.accountType;
    }

    void join(CustomerJoinEntity other) {
        Preconditions.checkNotNull(other, "待合并的账户信息不可以为空...");
        if (isSameChannle(other)) return;
        Optional<Long> this_id = getIdByChannel(other.accountType);
        Optional<Long> that_id = other.getIdByChannel(this.accountType);
        if (this_id.isPresent() || that_id.isPresent()) {
            Preconditions.checkArgument(other.getId().equals(this_id.orElse(-1L)), "账户%s已经绑定其他会员....", this);
            Preconditions.checkArgument(this.getId().equals(that_id.orElse(-1L)), "账户%s已经绑定其他会员....", other);
        }
        this.merge(other);
        other.merge(this);
    }

    public void unjoin(CustomerJoinEntity other) {
        Preconditions.checkNotNull(other, "待分离的账户信息不可以为空...");
        if (isSameChannle(other)) return;
        this.unmerge(other);
        other.unmerge(this);
    }

    private void unmerge(CustomerJoinEntity other) {
        if (other.isMember()) {
            if (null != this.memberId) Preconditions.checkState(this.memberId.equals(other.getId()), "移除对象与持有者不匹配...");
            this.memberId = null;
        }
        if (other.isWechat()) {
            if (null != this.weixinId) Preconditions.checkState(this.weixinId.equals(other.getId()), "移除对象与持有者不匹配...");
            this.weixinId = null;
        }
    }

    private void merge(CustomerJoinEntity other) {
        if (other.isMember()) this.memberId = other.getId();
        if (other.isWechat()) this.weixinId = other.getId();
    }

    Optional<Long> getIdByChannel(Channel channel) {
        if (channel == this.accountType) return Optional.of(this.getId());
        if (channel == Channel.TYPE_WEIXIN) return Optional.ofNullable(this.weixinId);
        if (channel == Channel.TYPE_MEMBER) return Optional.ofNullable(this.memberId);
        throw new IllegalArgumentException(String.format("不支持该类型渠道 %s", channel));
    }

    public int accountSize() {
        int size = 1;
        if (null != weixinId) size += 1;
        if (null != memberId) size += 1;
        if (null != publicId) size += 1;
        return size;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> map = super.toParamMap("accountType", "customerId");
        map.put("accountType", this.accountType.getVal());
        return map;
    }

    public List<CustomerId> getCustomerIds() {
        List<CustomerId> list = Lists.newArrayListWithCapacity(8);
        list.add(this.customerId);
        if (null != memberId)
            list.add(new CustomerId(memberId, Channel.TYPE_MEMBER, this.getStoreId()));
        if (null != weixinId)
            list.add(new CustomerId(weixinId, Channel.TYPE_WEIXIN, this.getStoreId()));
        return list;
    }

    public boolean isWechat() {
        return Channel.TYPE_WEIXIN == this.accountType;
    }

    public boolean isMember() {
        return Channel.TYPE_MEMBER == this.accountType;
    }


    public Optional<Long> getPublicId() {
        return Optional.ofNullable(publicId);
    }

    public Optional<Long> getMemberId() {
        return Optional.ofNullable(memberId);
    }

    public Optional<Long> getWeixinId() {
        return Optional.ofNullable(this.weixinId);
    }


    public Optional<Long> getRawMemberId() {
        return isMember() ? Optional.of(this.getId()) : Optional.ofNullable(memberId);
    }

    public Optional<Long> getRawWeixinId() {
        return isWechat() ? Optional.of(this.getId()) : Optional.ofNullable(weixinId);
    }


    public Long getStoreId() {
        return storeId;
    }

    public List<Map<String, Object>> toParams() {
        List<CustomerId> acts = getCustomerIds();
        return acts.stream().map(CustomerId::toParamMap).collect(Collectors.toList());
    }

    public boolean equalsWithAct(CustomerId account) {
        if (null == account) return false;
        return accountType == account.getChannel() &&
                Objects.equal(this.getId(), account.getId()) &&
                Objects.equal(storeId, account.getStoreId());
    }

    public boolean equalsByCustomerId(CustomerJoinEntity that) {
        if (this.equals(that)) return true;
        if (that.isMember()) {
            return that.getId().equals(this.getMemberId().orElse(null));
        } else if (that.isWechat()) {
            return that.getId().equals(this.getWeixinId().orElse(null));
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CustomerJoinEntity that = (CustomerJoinEntity) o;
        return accountType == that.accountType &&
                Objects.equal(publicId, that.publicId) &&
                Objects.equal(memberId, that.memberId) &&
                Objects.equal(weixinId, that.weixinId) &&
                Objects.equal(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), publicId, memberId, weixinId, storeId, accountType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("publicId", publicId)
                .add("memberId", memberId)
                .add("weixinId", weixinId)
                .add("storeId", storeId)
                .add("accountType", accountType.getVal())
                .toString();
    }
}
