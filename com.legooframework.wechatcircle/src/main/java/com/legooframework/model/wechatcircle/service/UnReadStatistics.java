package com.legooframework.model.wechatcircle.service;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

public class UnReadStatistics {
    private final String ownerWxId;
    private final int[] statistics;

    UnReadStatistics(String ownerWxId) {
        this.ownerWxId = ownerWxId;
        this.statistics = new int[5];
        Arrays.fill(statistics, 0);
    }

    boolean equalsWx(String wxId) {
        return StringUtils.equals(this.ownerWxId, wxId);
    }

    void fill(Map<String, Object> data) {
        this.statistics[0] = MapUtils.getIntValue(data, "WechatCircle", 0);
        this.statistics[1] = MapUtils.getIntValue(data, "Liked", 0);
        this.statistics[2] = MapUtils.getIntValue(data, "Comments", 0);
        this.statistics[3] = MapUtils.getIntValue(data, "Other", 0);
        this.statistics[4] = this.statistics[0] + this.statistics[1] + this.statistics[2] + this.statistics[3];
    }

    public String toViewMap() {
        // ownerWxId|WechatCircle|Liked|Comments|Other|Total
        return String.format("%s|%s|%s|%s|%s|%s", ownerWxId, this.statistics[0], this.statistics[1], this.statistics[2],
                this.statistics[3], this.statistics[4]);
    }

}
