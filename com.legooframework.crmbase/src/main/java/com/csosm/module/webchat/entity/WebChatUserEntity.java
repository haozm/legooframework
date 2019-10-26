package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntity;

import com.csosm.commons.entity.Replaceable;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class WebChatUserEntity extends BaseEntity<String> implements Replaceable {

    private String ownerUserName;
    private String userName;
    private String nickName;
    private int type;
    private String iconUrl;
    private String conRemark;
    private Integer storeId, companyId;
    private Integer bildMemberId;

    WebChatUserEntity(String ownerUserName, String userName, String nickName, int type, String iconUrl,
                      String conRemark, Integer storeId, Integer companyId, Integer memberId) {
        super(userName);
        this.ownerUserName = ownerUserName;
        this.userName = userName;
        this.nickName = nickName;
        this.type = type;
        this.bildMemberId = memberId;
        this.iconUrl = iconUrl;
        this.conRemark = conRemark;
        this.storeId = storeId;
        this.companyId = companyId;
    }

    public Optional<WebChatUserEntity> bildMember(MemberEntity member) {
        Preconditions.checkNotNull(member);
        if (null != bildMemberId && bildMemberId.equals(member.getId())) return Optional.absent();
        try {
            WebChatUserEntity clone = (WebChatUserEntity) this.clone();
            clone.bildMemberId = member.getId();
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getBildMemberId() {
        return bildMemberId;
    }

    public boolean hasMember() {
        return null != bildMemberId;
    }

    public boolean isbildMember(MemberEntity member) {
        if (null == bildMemberId) return false;
        return bildMemberId.equals(member.getId());
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isOwnStore(StoreEntity store) {
        return this.storeId.intValue() == store.getId().intValue();
    }

    public String getUserName() {
        return userName;
    }


    public String getOwnerUserName() {
        return ownerUserName;
    }

    public Optional<String> getNickName() {
        return Optional.fromNullable(nickName);
    }

    public int getType() {
        return type;
    }

    public Optional<String> getIconUrl() {
        return Optional.fromNullable(iconUrl);
    }

    public Optional<String> getConRemark() {
        return Optional.fromNullable(conRemark);
    }

    /**
     * 判断该微信账号是否是主微信号
     *
     * @return
     */
    public boolean isOwner() {
        return StringUtils.equals(this.userName, this.ownerUserName);
    }

    @Override
    public Map<String, String> toSmsMap(StoreEntity store) {
        Map<String, String> map = Maps.newHashMap();
        map.put("{微信用户}", this.getNickName().or(""));
        map.put("{微信账号}", this.userName == null ? "" : this.userName);
        map.put("{微信备注}", this.conRemark == null ? "" : this.conRemark);
        return map;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("userName", userName);
        map.put("weixinId", getId());
        map.put("nickName", nickName);
        map.put("name", nickName);
        map.put("type", type);
        map.put("iconUrl", iconUrl);
        map.put("conRemark", conRemark);
        // 是否绑定 会员
        map.put("isBildMember", null != bildMemberId);
        // 如果绑定则输出 绑定会会员的ID
        map.put("memberId", bildMemberId);
        map.put("birthday", "");
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebChatUserEntity)) return false;
        if (!super.equals(o)) return false;
        WebChatUserEntity that = (WebChatUserEntity) o;
        return type == that.type &&
                Objects.equal(userName, that.userName) &&
                Objects.equal(nickName, that.nickName) &&
                Objects.equal(iconUrl, that.iconUrl) &&
                Objects.equal(conRemark, that.conRemark);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), userName, nickName, type, iconUrl, conRemark);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userName", userName)
                .add("nickName", nickName)
                .add("type", type)
                .add("iconUrl", iconUrl)
                .add("conRemark", conRemark)
                .toString();
    }

    public String toSimple() {
        return MoreObjects.toStringHelper(this)
                .add("userName", userName)
                .add("nickName", nickName)
                .add("conRemark", conRemark)
                .toString();
    }

}
