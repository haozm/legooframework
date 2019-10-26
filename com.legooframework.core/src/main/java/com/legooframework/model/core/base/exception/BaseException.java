package com.legooframework.model.core.base.exception;

import com.google.common.base.MoreObjects;

import java.util.Optional;

public abstract class BaseException extends RuntimeException {

    private final String errCode;

    public BaseException(String errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public BaseException(String errCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public Optional<Throwable> getOriginal() {
        return Optional.ofNullable(super.getCause());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("errCode", errCode)
                .add("message", getMessage())
                .add("cause", getCause())
                .toString();
    }
}
