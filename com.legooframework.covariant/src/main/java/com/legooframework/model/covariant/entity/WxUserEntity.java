package com.legooframework.model.covariant.entity;


import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class WxUserEntity extends BaseEntity<String> implements ToReplace {

    private Integer storeId, companyId;
    private String userName, nickName, iconUrl, conRemark, bindWxUserName, devicesId;
    private int type;

    WxUserEntity(String id, ResultSet res) {
        super(id);
        try {
            this.userName = ResultSetUtil.getString(res, "userName");
            this.bindWxUserName = ResultSetUtil.getString(res, "bindWxUserName");
            this.conRemark = ResultSetUtil.getOptString(res, "conRemark", null);
            this.nickName = ResultSetUtil.getOptString(res, "nickName", null);
            this.devicesId = ResultSetUtil.getString(res, "fromDevicesId");
            this.iconUrl = ResultSetUtil.getOptString(res, "iconUrl", null);
            this.type = ResultSetUtil.getOptObject(res, "type", Long.class).orElse(0L).intValue();
        } catch (SQLException e) {
            throw new RuntimeException("Restore WxUserEntity has SQLException", e);
        }
    }

    void setAddInfo(Integer companyId, Integer storeId) {
        this.companyId = companyId;
        this.storeId = storeId;
    }

    public String getBindWxUserName() {
        return bindWxUserName;
    }

    public String getDevicesId() {
        return devicesId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getUserName() {
        return userName;
    }

    public Optional<String> getNickName() {
        return Optional.ofNullable(nickName);
    }

    public Optional<String> getIconUrl() {
        return Optional.ofNullable(iconUrl);
    }

    public Optional<String> getConRemark() {
        return Optional.ofNullable(conRemark);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("userName", userName);
        params.put("devicesId", devicesId);
        params.put("bindWxUserName", bindWxUserName);
        return params;
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("微信昵称", Strings.nullToEmpty(this.nickName));
        map.put("微信备注", Strings.nullToEmpty(this.conRemark));
        map.put("微信账号", this.userName);
        return map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("userName", userName)
                .add("nickName", nickName)
                .add("type", type)
                .add("iconUrl", iconUrl)
                .add("conRemark", conRemark)
                .toString();
    }
}
