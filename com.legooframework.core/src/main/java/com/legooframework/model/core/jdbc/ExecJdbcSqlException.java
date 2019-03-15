package com.legooframework.model.core.jdbc;

import com.legooframework.model.core.base.exception.BaseException;

public class ExecJdbcSqlException extends BaseException {

    public ExecJdbcSqlException(String message) {
        super("1020", message);
    }

    public ExecJdbcSqlException(String message, Throwable cause) {
        super("1020", message, cause);
    }

}
