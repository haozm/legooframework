package com.legooframework.model.dict.dto;

import com.google.common.collect.Lists;
import com.legooframework.model.dict.entity.KvDictEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

// 一组类型字段明细的聚合
public class KvTypeDictDto {

    private final List<KvDictDto> items;
    private final String type;

    public KvTypeDictDto(List<KvDictEntity> items, String type) {
        this.items = Lists.newArrayList();
        items.forEach(x -> this.items.add(x.createDto()));
        this.type = type;
    }

    public List<KvDictDto> getItems() {
        return items;
    }

    public Optional<KvDictDto> valueOf(String value) {
        return items.stream().filter(x -> StringUtils.equals(value, x.getValue())).findFirst();
    }

}
