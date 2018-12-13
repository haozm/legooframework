package com.legooframework.model.usergroups.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.customer.entity.Channel;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

public class MemberDto {
    private final Long id, storeId;
    private final Channel channel;
    private String wxId;
    private final String name, shortName, iconUrl, phoneNo, remark, shortRemark;
    
    public MemberDto(Long id, Long storeId, Channel channel, String name, String shortName, String iconUrl, String phoneNo,
                     String remark, String shortRemark) {
        super();
        this.id = id;
        this.storeId = storeId;
        this.channel = channel;
        this.name = name;
        this.shortName = shortName;
        this.iconUrl = iconUrl;
        this.phoneNo = phoneNo;
        this.remark = remark;
        this.shortRemark = shortRemark;
    }

    public MemberDto(Map<String, Object> params) {
        this.id = MapUtils.getLong(params, "userId");
        this.storeId = MapUtils.getLong(params, "storeId");
        this.channel = Channel.valueOf(MapUtils.getIntValue(params, "type"));
        this.wxId = MapUtils.getString(params, "userRawId");
        this.name = MapUtils.getString(params, "userName");
        this.shortName = MapUtils.getString(params, "userNameShort");
        this.iconUrl = MapUtils.getString(params, "iconUrl");
        this.phoneNo = MapUtils.getString(params, "phoneNum");
        this.remark = MapUtils.getString(params, "userRemark");
        this.shortRemark = MapUtils.getString(params, "userRemarkShort");
    }

    public Long getId() {
        return id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getRemark() {
        return remark;
    }

    public String getShortRemark() {
        return shortRemark;
    }

    public boolean isMember() {
    	return this.channel == Channel.TYPE_MEMBER;
    }
    
    public boolean isWechat() {
    	return this.channel == Channel.TYPE_WEIXIN;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberDto memberDto = (MemberDto) o;
        return channel == memberDto.channel &&
                Objects.equal(id, memberDto.id) &&
                Objects.equal(storeId, memberDto.storeId) &&
                Objects.equal(name, memberDto.name) &&
                Objects.equal(shortName, memberDto.shortName) &&
                Objects.equal(iconUrl, memberDto.iconUrl) &&
                Objects.equal(phoneNo, memberDto.phoneNo) &&
                Objects.equal(remark, memberDto.remark) &&
                Objects.equal(shortRemark, memberDto.shortRemark);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, storeId, channel, name, shortName, iconUrl, phoneNo, remark, shortRemark);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("storeId", storeId)
                .add("channel", channel)
                .add("name", name)
                .add("shortName", shortName)
                .add("iconUrl", iconUrl)
                .add("phoneNo", phoneNo)
                .add("remark", remark)
                .add("shortRemark", shortRemark)
                .toString();
    }

	public String getWxId() {
		return wxId;
	}
}
