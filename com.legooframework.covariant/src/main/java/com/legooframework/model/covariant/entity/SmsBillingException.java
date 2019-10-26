package com.legooframework.model.covariant.entity;

import com.legooframework.model.core.base.exception.BaseException;

public class SmsBillingException extends BaseException {

    public SmsBillingException(String message) {
        super("SMS_0100", message);
    }
}
