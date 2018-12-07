package com.legooframework.model.base.exception;

public class UnKonwedException extends BaseException {

    public UnKonwedException(Class<?> clazz, String method, Throwable cause) {
        super("9999", String.format("%s.%s has UnKonwedException", clazz, method), cause);
    }
}
