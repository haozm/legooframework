package com.csosm.module.webocx.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

import java.util.Map;

public class MemberGroupInfo {

    private String groupId, title;
    private long count;
    private Map<String, Object> params;
    private int index;

    MemberGroupInfo(String groupId, int index, String tilte, long count, Map<String, Object> params) {
        this.groupId = groupId;
        this.index = index;
        this.title = tilte;
        this.count = count;
        this.params = params;
    }

    void setCount(long count) {
        this.count = count;
    }

    public String getId() {
        return null;
    }

    public String getTitle() {
        return title;
    }

    public long getCount() {
        return count;
    }

    int getIndex() {
        return index;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("groupId", groupId);
        param.put("title", title);
        param.put("params", params);
        param.put("count", count);
        return param;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("groupId", groupId)
                .add("title", title)
                .add("params", params)
                .add("count", count)
                .toString();
    }
}
