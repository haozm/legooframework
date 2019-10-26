package com.legooframework.model.statistical.entity.rules;

import com.google.common.collect.Lists;
import com.legooframework.model.statistical.entity.FieldMetaRefEntity;

import java.util.List;

abstract class FieldMetaRefSupport {
    List<FieldMetaRefEntity> fieldMetaRefs = Lists.newArrayList();

    void addFieldMetaRef(FieldMetaRefEntity fieldMetaRef) {
        fieldMetaRefs.add(fieldMetaRef);
    }
}
