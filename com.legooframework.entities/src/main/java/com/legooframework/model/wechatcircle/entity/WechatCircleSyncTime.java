package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;

public class WechatCircleSyncTime {

    private String weixinId, syncType;
    private long startTime, lastTime;

    public WechatCircleSyncTime(String weixinId, String syncType, long startTime, long lastTime) {
        this.weixinId = weixinId;
        this.syncType = syncType;
        this.startTime = startTime;
        this.lastTime = lastTime;
    }

    public boolean hasLastTime() {
        return this.lastTime != 0L;
    }

    public String getWeixinId() {
        return weixinId;
    }

    public String getSyncType() {
        return syncType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("weixinId", weixinId)
                .add("syncType", syncType)
                .add("startTime", startTime)
                .add("lastTime", lastTime)
                .toString();
    }
}
