package com.legooframework.model.commons.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TreeStructure {

    Object getId();

    Object getPid();

    String getLabel();

    Object getRawId();

    Map<String, Object> getAttachData();

    boolean hasChild();

    List<TreeStructure> getChildren();

    Optional<List<TreeStructure>> getAllChildren();

    boolean isLogicRoot();

    void addChild(TreeStructure child);

    Map<String, Object> toMap();

    Optional<TreeStructure> getChildById(Object id);
}
