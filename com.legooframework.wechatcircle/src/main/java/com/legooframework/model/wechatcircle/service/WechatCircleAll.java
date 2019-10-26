package com.legooframework.model.wechatcircle.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.wechatcircle.entity.CircleCommentEntity;
import com.legooframework.model.wechatcircle.entity.WechatCircleEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WechatCircleAll {

    private WechatCircleEntity wechatCircle;
    private List<CircleCommentEntity> comments;
    private final boolean unread;

    WechatCircleAll(WechatCircleEntity wechatCircle, List<CircleCommentEntity> comments, boolean unread) {
        this.wechatCircle = wechatCircle;
        this.comments = comments;
        this.unread = unread;
    }

    Collection<String> getWeixinIds() {
        Set<String> _ids = Sets.newHashSet();
        _ids.add(wechatCircle.getWeixinId());
        if (CollectionUtils.isNotEmpty(comments)) {
            comments.stream().map(CircleCommentEntity::getWeixinIds).forEach(_ids::addAll);
        }
        return _ids;
    }

    public boolean isUnread() {
        return unread;
    }

    void filter(String ownerWxId) {
        if (CollectionUtils.isNotEmpty(comments)) {
            this.comments = comments.stream().filter(x -> x.isOwner(ownerWxId)).collect(Collectors.toList());
        }
    }

    Map<String, Object> toViewMap() {
        Map<String, Object> params = wechatCircle.toViewMap();
        if (CollectionUtils.isNotEmpty(comments)) {
            List<CircleCommentEntity> liked = comments.stream().filter(CircleCommentEntity::isLiked).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(liked)) {
                params.put("likedSize", liked.size());
                params.put("likedUsernames", liked.stream().map(CircleCommentEntity::toViewMap).collect(Collectors.toList()));
            }
            List<CircleCommentEntity> cmts = comments.stream().filter(x -> !x.isLiked()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(cmts)) {
                params.put("comments", cmts.stream().map(CircleCommentEntity::toViewMap).collect(Collectors.toList()));
            }
        }
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("circleId", wechatCircle.getId())
                .add("weixinId", wechatCircle.getWeixinId())
                .toString();
    }
}
