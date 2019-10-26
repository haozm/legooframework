package com.legooframework.model.wechatcircle.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WechatCircleAllSet {

    private final String ownerWxId;
    private final List<String> friendWxIds;
    private final List<WechatCircleAll> circleDetails;

    WechatCircleAllSet(String ownerWxId, List<WechatCircleAll> circleDetails) {
        this.ownerWxId = ownerWxId;
        this.circleDetails = circleDetails;
        this.circleDetails.forEach(x -> x.filter(ownerWxId));
        Set<String> _set = Sets.newHashSet();
        circleDetails.stream().map(WechatCircleAll::getWeixinIds).forEach(_set::addAll);
        this.friendWxIds = Lists.newArrayList(_set);
    }

    public int getSize() {
        return CollectionUtils.isEmpty(circleDetails) ? 0 : circleDetails.size();
    }

    public Map<String, Object> toViewMap01() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("friendWxIds", StringUtils.join(friendWxIds, ','));
        params.put("ownerWxId", ownerWxId);
        List<Map<String, Object>> _data = circleDetails.stream().map(WechatCircleAll::toViewMap)
                .collect(Collectors.toList());
        params.put("wechatCircleData", WebUtils.toJson(_data));
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ownerWxId", ownerWxId)
                .add("friendWxIds", friendWxIds)
                .add("circleDetails's size ", CollectionUtils.isEmpty(circleDetails) ? 0 : circleDetails.size())
                .toString();
    }
}
