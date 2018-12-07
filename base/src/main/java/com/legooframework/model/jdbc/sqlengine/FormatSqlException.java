package com.legooframework.model.jdbc.sqlengine;

import com.legooframework.model.base.exception.BaseException;

public class FormatSqlException extends BaseException {

    public FormatSqlException(String message) {
        super("2000", message);
    }

    public FormatSqlException(String message, Throwable cause) {
        super("2000", message, cause);
    }

}
