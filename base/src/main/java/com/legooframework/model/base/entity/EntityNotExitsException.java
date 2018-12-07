package com.legooframework.model.base.entity;

import com.legooframework.model.base.exception.BaseException;

public class EntityNotExitsException extends BaseException {

    public EntityNotExitsException(Class<? extends BaseEntity> clazz, Object id) {
        super("1040", String.format("Entity %s not exits which id= %s", clazz, id));
    }

}
