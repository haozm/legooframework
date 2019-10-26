package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class EWeiShopMemberEntity extends BaseEntity<Integer> implements ToReplace {
    private Integer uniacid, uid, status, agentid;
    private String openId, nickname, realname, mobile;
    private boolean isAgent;

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("uid", uid);
        params.put("shopMemberId", getId());
        params.put("uniacid", uniacid);
        params.put("status", status);
        params.put("openid", openId);
        params.put("nickname", nickname);
        params.put("mobile", mobile);
        params.put("agentid", agentid);
        params.put("realname", realname);
        return params;
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("粉丝昵称", nickname);
        return params;
    }

    EWeiShopMemberEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.uniacid = res.getInt("uniacid");
            this.openId = res.getString("openid");
            this.nickname = res.getString("nickname");
            this.mobile = res.getString("mobile");
            this.realname = res.getString("realname");
            this.agentid = res.getInt("agentid");
            this.uniacid = res.getInt("uniacid");
            this.uid = res.getInt("uid");
            this.status = res.getInt("status");
            this.isAgent = res.getInt("isagent") == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Restore EWeiShopMemberEntity has SQLException", e);
        }
    }

    Integer getUniacid() {
        return uniacid;
    }

    public String getOpenId() {
        return openId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EWeiShopMemberEntity that = (EWeiShopMemberEntity) o;
        return isAgent == that.isAgent &&
                Objects.equal(getId(), that.getId()) &&
                Objects.equal(uniacid, that.uniacid) &&
                Objects.equal(uid, that.uid) &&
                Objects.equal(status, that.status) &&
                Objects.equal(openId, that.openId) &&
                Objects.equal(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), uniacid, uid, status, openId, nickname, isAgent);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("uniacid", uniacid)
                .add("uid", uid)
                .add("status", status)
                .add("openId", openId)
                .add("nickname", nickname)
                .add("isAgent", isAgent)
                .toString();
    }
}
