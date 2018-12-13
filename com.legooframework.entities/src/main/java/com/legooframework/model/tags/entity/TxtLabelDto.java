package com.legooframework.model.tags.entity;

import com.google.common.collect.Lists;

import java.util.List;

public class TxtLabelDto {

    private final Long id, pId;
    private final String labelName, labelDesc;
    private final List<TxtLabelDto> children;

    TxtLabelDto(Long id, Long pId, String labelName, String labelDesc) {
        this.id = id;
        this.pId = pId;
        this.labelName = labelName;
        this.labelDesc = labelDesc;
        this.children = Lists.newArrayList();
    }

    public void addChild(TxtLabelDto child) {
        this.children.add(child);
    }

    public List<TxtLabelDto> getChildren() {
        return children;
    }

    public Long getId() {
        return id;
    }

    public Long getpId() {
        return pId;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getLabelDesc() {
        return labelDesc;
    }
}
