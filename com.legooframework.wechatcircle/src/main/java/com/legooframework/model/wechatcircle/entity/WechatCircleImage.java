package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class WechatCircleImage extends BaseEntity<String> implements BatchSetter {

    private Long ownerId;
    private String circleId;
    private String imgUrl;
    private String simpleUrl;
    private final int order;

    void setCircle(Long ownerId, String circleId) {
        this.circleId = circleId;
        this.ownerId = ownerId;
    }

    public WechatCircleImage(String imgId, String imgUrl, String simpleUrl, int order) {
        super(imgId);
        this.imgUrl = imgUrl;
        this.simpleUrl = simpleUrl;
        this.order = order;
    }

    void setImgUrl(String imgUrl) {
        if (StringUtils.isEmpty(this.imgUrl))
            this.imgUrl = imgUrl;
    }

    void setSimpleUrl(String simpleUrl) {
        if (StringUtils.isEmpty(this.simpleUrl))
            this.simpleUrl = simpleUrl;
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("imgUrl", Strings.isNullOrEmpty(imgUrl) || StringUtils.equals("NULL", imgUrl) ? "null" : imgUrl);
        params.put("simpleUrl", Strings.isNullOrEmpty(simpleUrl) || StringUtils.equals("NULL", simpleUrl) ? "null" : simpleUrl);
        return params;
    }

    boolean isComplete() {
        return StringUtils.isNotEmpty(this.imgUrl) && StringUtils.isNotEmpty(this.simpleUrl);
    }

    boolean isMissing() {
        return StringUtils.isEmpty(this.imgUrl) || StringUtils.isEmpty(this.simpleUrl);
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getId());
        ps.setObject(2, circleId);
        ps.setObject(3, imgUrl);
        ps.setObject(4, simpleUrl);
        ps.setObject(5, order);
        ps.setObject(6, ownerId);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("circleId", circleId);
        params.put("ownerId", ownerId);
        params.put("imgUrl", imgUrl);
        params.put("simpleUrl", simpleUrl);
        params.put("imgOrder", order);
        return params;
    }

    String getImgUrl() {
        return imgUrl;
    }

    String getSimpleUrl() {
        return simpleUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WechatCircleImage that = (WechatCircleImage) o;
        return Objects.equal(getId(), that.getId()) && Objects.equal(circleId, that.circleId)
                && Objects.equal(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), circleId, ownerId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("circleId", circleId)
                .add("ownerId", ownerId)
                .add("imgUrl", imgUrl)
                .add("simpleUrl", simpleUrl)
                .add("order", order)
                .toString();
    }
}
