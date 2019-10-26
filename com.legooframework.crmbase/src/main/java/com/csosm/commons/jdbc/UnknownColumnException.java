package com.csosm.commons.jdbc;

public class UnknownColumnException extends RuntimeException {

    public UnknownColumnException(String message, Throwable cause) {
        super(message, cause);
    }
}
