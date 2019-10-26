package com.legooframework.model.takecare.entity;

import com.legooframework.model.core.base.exception.BaseException;

public class CareNinetyProcessException extends BaseException {

    CareNinetyProcessException(String errCode, String message) {
        super(errCode, message);
    }
}
