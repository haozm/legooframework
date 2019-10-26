package com.legooframework.model.dict.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class KvDictDto {

    private final String value, name, type;
    private final int index;

    public KvDictDto( String value, String name, int index, String type) {
        this.value = value;
        this.name = name;
        this.index = index;
        this.type = type;
    }

    public KvDictDto(String paramsRes) {
    	Preconditions.checkArgument(!Strings.isNullOrEmpty(paramsRes), "入参res不能为空");
    	String[] params = paramsRes.split(":");
    	this.type = params[0];
    	this.value = params[1];
    	this.name = params[2];
    	this.index = Integer.parseInt(params[3]);
    }
    
    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("name", name)
                .add("index", index)
                .add("type", type)
                .toString();
    }
}
