package com.csosm.module.base.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

public class WeixinAct {
    private final String weixinId;
    private final String nickName;
    private final String iconUrl;
    private final String remark;

    WeixinAct(String weixinId, String nickName, String iconUrl, String remark) {
        this.weixinId = weixinId;
        this.nickName = StringUtils.equals(nickName, "-") ? null : nickName;
        this.iconUrl = StringUtils.equals(iconUrl, "-") ? null : iconUrl;
        this.remark = StringUtils.equals(remark, "-") ? null : remark;
    }

    public String getWeixinId() {
        return weixinId;
    }

    public String getNickName() {
        return nickName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getRemark() {
        return remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeixinAct weixinAct = (WeixinAct) o;
        return Objects.equal(weixinId, weixinAct.weixinId) &&
                Objects.equal(nickName, weixinAct.nickName) &&
                Objects.equal(iconUrl, weixinAct.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(weixinId, nickName, iconUrl);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("weixinId", weixinId)
                .add("nickName", nickName)
                .add("iconUrl", iconUrl)
                .toString();
    }
}
