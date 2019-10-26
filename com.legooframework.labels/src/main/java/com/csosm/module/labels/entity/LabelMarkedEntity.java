package com.csosm.module.labels.entity;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LabelMarkedEntity extends BaseEntity<Integer> {

    private Long labelId;
    private Integer memberId;
    private String weixinId;
    private Integer companyId, storeId;

    LabelMarkedEntity(Long labelId, Integer memberId, String weixinId, StoreEntity store) {
        super(0, 0, new Date());
        Preconditions.checkNotNull(labelId);
        Preconditions.checkArgument(store.getCompanyId().isPresent());
        this.labelId = labelId;
        Preconditions.checkState(memberId != null || weixinId != null, "微信 或会员指定其一...");
        this.memberId = memberId;
        this.weixinId = weixinId;

        this.companyId = store.getCompanyId().get();
        this.storeId = store.getId();
    }

    LabelMarkedEntity(Long labelId, WebChatUserEntity wechat) {
        super(0, 0, new Date());
        Preconditions.checkNotNull(labelId);
        this.labelId = labelId;
        this.memberId = wechat.getBildMemberId();
        this.weixinId = wechat.getUserName();
        this.companyId = wechat.getCompanyId();
        this.storeId = wechat.getStoreId();
    }

    LabelMarkedEntity(LabelNodeEntity label, MemberEntity member, WebChatUserEntity webChatUser,
                      Object createUserId) {
        super(0, createUserId, new Date());
        Preconditions.checkNotNull(label);
        this.labelId = label.getId();
        Preconditions.checkState(member != null || webChatUser != null, "微信 或会员指定其一...");
        if (member != null) {
            this.memberId = member.getId();
            this.companyId = member.getCompanyId();
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(member.getStoreIds()),
                    "当前会员%s无门店信息....", member.getName());
            List<Integer> _ids = Lists.newArrayList(member.getStoreIds());
            this.storeId = _ids.get(0);
        }
        if (webChatUser != null) {
            this.weixinId = webChatUser.getId();
            this.companyId = webChatUser.getCompanyId();
            this.storeId = webChatUser.getStoreId();
            if (webChatUser.hasMember()) {
                this.memberId = webChatUser.getBildMemberId();
                if (null != member)
                    Preconditions.checkState(webChatUser.isbildMember(member));
            }

        }
    }

    LabelMarkedEntity(ResultSet res) throws SQLException {
        super(0);
        this.labelId = res.getLong("labelId");
        this.memberId = res.getObject("memberId") != null ? res.getInt("memberId") : null;
        this.weixinId = res.getString("weixinId");
        this.storeId = res.getInt("storeId");
        this.companyId = res.getInt("companyId");
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        data.put("labelId", labelId);
        data.put("memberId", memberId);
        data.put("weixinId", weixinId);
        data.put("storeId", storeId);
        data.put("companyId", companyId);
        return data;
    }

    public boolean hasMember() {
        return memberId != null;
    }

    public boolean hasWeixin() {
        return weixinId != null;
    }

    public boolean hasAll() {
        return memberId != null && weixinId != null;
    }

    public Long getLabelId() {
        return labelId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public String getWeixinId() {
        return weixinId;
    }

    public List<LabelMarkedEntity> spliter(LoginUserContext user) {
        List<LabelMarkedEntity> list = Lists.newArrayListWithCapacity(2);

        if (hasAll()) {
            try {
                LabelMarkedEntity wx = (LabelMarkedEntity) this.clone();
                wx.memberId = null;
                wx.setCreateUserId(user.getUserId());
                list.add(wx);
                LabelMarkedEntity mm = (LabelMarkedEntity) this.clone();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelMarkedEntity that = (LabelMarkedEntity) o;
        return Objects.equal(labelId, that.labelId) &&
                Objects.equal(memberId, that.memberId) &&
                Objects.equal(weixinId, that.weixinId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), labelId, memberId, weixinId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("labelId", labelId)
                .add("memberId", memberId)
                .add("weixinId", weixinId)
                .toString();
    }
}
