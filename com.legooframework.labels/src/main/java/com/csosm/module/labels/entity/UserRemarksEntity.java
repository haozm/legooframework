package com.csosm.module.labels.entity;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserRemarksEntity extends BaseEntity<Long> {

    private Integer memberId, storeId, companyId;
    private String weixinId, remarks;
    private int type;

    UserRemarksEntity(LoginUserContext userContext, MemberEntity member, WebChatUserEntity wechatUser, String remarks) {
        super(0L, userContext.getUserId(), DateTime.now().toDate());
        Preconditions.checkArgument((null != member || null != wechatUser), "会员 或者 微信需指定其中之一...");
        this.memberId = member != null ? member.getId() : null;
        this.weixinId = wechatUser != null ? wechatUser.getUserName() : null;
        if (wechatUser != null && wechatUser.hasMember()) {
            this.memberId = wechatUser.getBildMemberId();
        }
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarks), "备注信息不可以为空...");
        this.remarks = remarks;
        Preconditions.checkArgument(userContext.getStore().isPresent(), "用户%s 无门店属性...", userContext.getUsername());
        this.storeId = userContext.getStore().get().getId();
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "用户%s 无公司属性...", userContext.getUsername());
        this.companyId = userContext.getCompany().get().getId();
    }

    UserRemarksEntity(Integer createUserId, String remarks, WebChatUserEntity wechat) {
        super(0L, createUserId, new Date());
        this.remarks = remarks;
        this.memberId = wechat.getBildMemberId();
        this.weixinId = wechat.getUserName();
        this.companyId = wechat.getCompanyId();
        this.storeId = wechat.getStoreId();
    }

    UserRemarksEntity(ResultSet res) throws SQLException {
        super(res.getLong("id"), res.getObject("createUserId"), res.getDate("createTime"));
        this.memberId = res.getObject("memberId") == null ? null : res.getInt("memberId");
        this.weixinId = res.getString("weixinId");
        this.storeId = res.getInt("storeId");
        this.companyId = res.getInt("companyId");
        this.remarks = res.getString("remarks");
        this.type = res.getInt("type");
    }

    UserRemarksEntity createByClone(LoginUserContext userContext, String remarks) {
        try {
            UserRemarksEntity clone = (UserRemarksEntity) this.clone();
            clone.remarks = remarks;
            clone.setCreateUserId(userContext.getUserId());
            return clone;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMember() {
        return null != memberId;
    }

    public boolean hasWechat() {
        return null != weixinId;
    }

    public boolean isBindRemark() {
        return this.type == 1;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> params = super.toMap();
        params.put("memberId", memberId);
        params.put("storeId", storeId);
        params.put("companyId", companyId);
        params.put("weixinId", weixinId);
        params.put("remarks", remarks);
        return params;
    }

    public Optional<Integer> getMemberId() {
        return Optional.fromNullable(memberId);
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Optional<String> getWeixinId() {
        return Optional.fromNullable(weixinId);
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRemarksEntity that = (UserRemarksEntity) o;
        return Objects.equal(memberId, that.memberId) &&
                Objects.equal(getCreateUserId(), that.getCreateUserId()) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(weixinId, that.weixinId) &&
                Objects.equal(remarks, that.remarks);
    }

    public List<UserRemarksEntity> spliter(LoginUserContext user) {
        List<UserRemarksEntity> list = Lists.newArrayListWithCapacity(2);
        if (hasMember() && hasWechat()) {
            try {
                UserRemarksEntity wx = (UserRemarksEntity) this.clone();
                wx.memberId = null;
                wx.setCreateUserId(user.getUserId());
                list.add(wx);
                UserRemarksEntity mm = (UserRemarksEntity) this.clone();
                mm.weixinId = null;
                mm.setCreateUserId(user.getUserId());
                list.add(mm);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            list.add(this);
        }
        return list;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCreateUserId(), memberId, storeId, companyId, weixinId, remarks);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("createUserId", super.getCreateUserId())
                .add("memberId", memberId)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("weixinId", weixinId)
                .add("remarks", remarks)
                .toString();
    }
}
