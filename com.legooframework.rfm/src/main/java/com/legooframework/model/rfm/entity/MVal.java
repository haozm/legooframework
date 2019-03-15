package com.legooframework.model.rfm.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import java.util.Map;

public class MVal extends AbstractVal {

    MVal(int val1, int val2, int val3, int val4) {
        Preconditions.checkState(val1 > val2 && val2 > val3 && val3 > val4, "错误的Monetary取值[%s,%s,%s,%s]",
                val1, val2, val3, val4);
        this.range5lv = Range.closed(val1, Integer.MAX_VALUE);
        this.range4th = Range.closedOpen(val2, val1);
        this.range3rd = Range.closedOpen(val3, val2);
        this.range2nd = Range.closedOpen(val4, val3);
        this.range1st = Range.closedOpen(0, val4);
    }

    @Override
    Map<String, Object> toMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("mV1", range5lv.lowerEndpoint());
        params.put("mV2", range4th.lowerEndpoint());
        params.put("mV3", range3rd.lowerEndpoint());
        params.put("mV4", range2nd.lowerEndpoint());
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Monetary")
                .add("super", super.toString())
                .toString();
    }
}
