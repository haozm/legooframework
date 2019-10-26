package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

public class WechatCircleTranDto {

    private WechatCircleEntity wechatCircle;
    private List<CircleCommentEntity> comments;
    private DataSourcesFrom sourcesFrom;

    public WechatCircleTranDto(WechatCircleEntity wechatCircle, List<CircleCommentEntity> comments,
                               DataSourcesFrom sourcesFrom) {
        this.wechatCircle = wechatCircle;
        this.comments = comments;
        this.sourcesFrom = sourcesFrom;
    }

    public long getSendTime() {
        return this.wechatCircle.getSendTime();
    }

    public WechatCircleEntity getWechatCircle() {
        return wechatCircle;
    }

    public String getWeixinId() {
        return wechatCircle.getWeixinId();
    }

    public Optional<List<CircleCommentEntity>> getComments() {
        return Optional.ofNullable(CollectionUtils.isEmpty(comments) ? null : comments);
    }

    public DataSourcesFrom getSourcesFrom() {
        return sourcesFrom;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("wechatCircle", wechatCircle)
                .add("comments' size", CollectionUtils.isEmpty(comments) ? 0 : comments.size())
                .add("sourcesFrom", sourcesFrom)
                .toString();
    }
}

