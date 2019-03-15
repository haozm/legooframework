package com.legooframework.model.rfm.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import java.util.Map;

public class RVal extends AbstractVal {

    RVal(int val1, int val2, int val3, int val4) {
        Preconditions.checkState(val1 < val2 && val2 < val3 && val3 < val4, "错误的Monetary取值[%s,%s,%s,%s]",
                val1, val2, val3, val4);
        this.range5lv = Range.closedOpen(1, val1);
        this.range4th = Range.closedOpen(val1, val2);
        this.range3rd = Range.closedOpen(val2, val3);
        this.range2nd = Range.closedOpen(val3, val4);
        this.range1st = Range.closed(val4, Integer.MAX_VALUE);
    }

    @Override
    Map<String, Object> toMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("rV1", range5lv.upperEndpoint());
        params.put("rV2", range4th.upperEndpoint());
        params.put("rV3", range3rd.upperEndpoint());
        params.put("rV4", range2nd.upperEndpoint());
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Recency")
                .add("super", super.toString())
                .toString();
    }
}
