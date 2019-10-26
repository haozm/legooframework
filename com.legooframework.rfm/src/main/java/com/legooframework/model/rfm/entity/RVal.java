package com.legooframework.model.rfm.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import java.util.Map;

public class RVal extends AbstractVal {
	
	private int val1;
	
	private int val2;
	
	private int val3;
	
	private int val4;
	
    RVal(int val1, int val2, int val3, int val4) {
        this.val1 = val1;
        this.val2 = val2;
        this.val3 = val3;
        this.val4 = val4;
    }

    @Override
    Map<String, Object> toMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("rV1", this.val1);
        params.put("rV2", this.val2);
        params.put("rV3", this.val3);
        params.put("rV4", this.val4);
        return params;
    }
    
    public Map<String,Object> toViewMap(){
    	 Map<String, Object> params = Maps.newHashMap();
         params.put("rV1", 0);
         params.put("rV2", this.val1);
         params.put("rV3", this.val2);
         params.put("rV4", this.val3);
         params.put("rV5", this.val4);
         return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Recency")
                .add("super", super.toString())
                .toString();
    }
}
