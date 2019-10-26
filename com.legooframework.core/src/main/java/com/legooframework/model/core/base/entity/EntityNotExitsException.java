package com.legooframework.model.core.base.entity;

import com.legooframework.model.core.base.exception.BaseException;

public class EntityNotExitsException extends BaseException {

    public EntityNotExitsException(Class<? extends BaseEntity> clazz, Object id) {
        super("1040", String.format("Entity %s not exits which id= %s", clazz, id));
    }

}
