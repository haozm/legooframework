package com.legooframework.model.customer.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.organization.entity.StoreEntity;
import com.legooframework.model.wechat.entity.WechatAccountEntity;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class CustomerId implements Serializable {

    private final Long id;
    private final Long storeId;
    private final Channel channel;

    public CustomerId(Long id, Channel channel, Long storeId) {
        Preconditions.checkNotNull(id, "CustomerId (Long id ...) 不可以为空...");
        Preconditions.checkNotNull(id, "CustomerId (Long storeId ...) 不可以为空...");
        this.channel = channel;
        this.id = id;
        this.storeId = storeId;
    }

    public CustomerId(ResultSet res) throws SQLException {
        this.channel = Channel.valueOf(res.getInt("accountType"));
        this.id = res.getLong("accountId");
        this.storeId = res.getLong("accountId");
    }

    CustomerId(WechatAccountEntity account, StoreEntity store) {
        this(account.getLongId(), Channel.TYPE_WEIXIN, store.getId());
    }

    public boolean isWechat() {
        return Channel.TYPE_WEIXIN == this.channel;
    }

    public boolean isMember() {
        return Channel.TYPE_MEMBER == this.channel;
    }

    public boolean isSameChannle(CustomerId that) {
        return this.channel == that.channel;
    }

    public boolean isSameStore(CustomerId that) {
        return this.storeId.equals(that.storeId);
    }

    public Long getId() {
        return id;
    }

    public Long getStoreId() {
        return storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerId that = (CustomerId) o;
        return channel == that.channel &&
                id.equals(that.id) &&
                storeId.equals(that.storeId);
    }

    public Channel getChannel() {
        return channel;
    }

    public Map<String, Object> toParamMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("accountType", channel.getVal());
        map.put("accountId", id);
        map.put("storeId", storeId);
        return map;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(channel, id, storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("channel", channel)
                .add("id", id)
                .add("storeId", storeId)
                .toString();
    }
}
