package com.legooframework.model.wechatcircle.mvc;

import com.legooframework.model.wechatcircle.entity.CircleSyncCycleEntity;
import org.apache.commons.lang3.StringUtils;

class CircleSyncCycleBuilder {
    
    private final int syncType;
    private String weixinId;
    private long minTime = 0, maxTime = 0;

    // 1 全量   2  单个
    CircleSyncCycleBuilder(String weixinId) {
        this.syncType = 1;
        this.weixinId = weixinId;
    }

    CircleSyncCycleBuilder() {
        this.syncType = 2;
    }

    void setData(String weixinId, long sendTime) {
        if (StringUtils.isEmpty(this.weixinId)) this.weixinId = weixinId;
        if (minTime == 0L) {
            this.minTime = sendTime;
        } else if (this.minTime >= sendTime) {
            this.minTime = sendTime;
        }
        if (maxTime == 0L) {
            this.maxTime = sendTime;
        } else if (this.maxTime <= sendTime) {
            this.maxTime = sendTime;
        }
    }

    CircleSyncCycleEntity builder() {
        if (syncType == 1) {
            return CircleSyncCycleEntity.createBatchCycle(weixinId, minTime, maxTime);
        }
        return CircleSyncCycleEntity.createSingleCycle(weixinId, minTime, maxTime);
    }
}
