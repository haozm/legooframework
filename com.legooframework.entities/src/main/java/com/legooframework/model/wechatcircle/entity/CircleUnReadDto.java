package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

public class CircleUnReadDto {

    private int wechatCircle = 0, liked = 0, comments = 0, other = 0, total = 0;
    private final DataSourcesFrom source;

    public CircleUnReadDto(DataSourcesFrom source, Map<String, Object> data) {
        this.source = source;
        this.wechatCircle = MapUtils.getIntValue(data, "WechatCircle", 0);
        this.liked = MapUtils.getIntValue(data, "Liked", 0);
        this.comments = MapUtils.getIntValue(data, "Comments", 0);
        this.other = MapUtils.getIntValue(data, "Other", 0);
        this.total = this.wechatCircle + this.liked + this.comments + this.other;
    }

    private CircleUnReadDto(DataSourcesFrom source, int wechatCircle, int liked, int comments, int other) {
        this.wechatCircle = wechatCircle;
        this.liked = liked;
        this.comments = comments;
        this.other = other;
        this.source = source;
        this.total = this.wechatCircle + this.liked + this.comments + this.other;
    }

    public static CircleUnReadDto deCoding(Map<String, Object> dataMap) {
        String source = MapUtils.getString(dataMap, "source");
        String statistics = MapUtils.getString(dataMap, "statistics");
        DataSourcesFrom ds = new DataSourcesFrom(source);
        Map<String, String> data = Splitter.on(',').withKeyValueSeparator('=').split(statistics);
        return new CircleUnReadDto(ds, MapUtils.getIntValue(data, "WechatCircle", 0), MapUtils.getIntValue(data, "Liked", 0),
                MapUtils.getIntValue(data, "Comments", 0), MapUtils.getIntValue(data, "Other", 0));
    }

    public Map<String, Object> enCoding() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("source", this.source.toString());
        Map<String, Integer> param = Maps.newHashMap();
        param.put("WechatCircle", this.wechatCircle);
        param.put("Liked", this.liked);
        param.put("Comments", this.comments);
        param.put("Other", this.other);
        params.put("statistics", Joiner.on(',').withKeyValueSeparator('=').join(param));
        return params;
    }

    public DataSourcesFrom getSource() {
        return source;
    }

    public void statistics() {
        this.total = this.wechatCircle + this.liked + this.comments + this.other;
    }

    public int getWechatCircle() {
        return wechatCircle;
    }

    public int getLiked() {
        return liked;
    }

    public int getComments() {
        return comments;
    }

    public int getOther() {
        return other;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("wechatCircle", wechatCircle);
        params.put("liked", liked);
        params.put("comments", comments);
        params.put("other", other);
        params.put("total", total);
        params.put("source", source.toString());
        params.put("type", "CircleUnRead");
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .add("wechatCircle", wechatCircle)
                .add("liked", liked)
                .add("comments", comments)
                .add("other", other)
                .add("total", total)
                .toString();
    }
}
