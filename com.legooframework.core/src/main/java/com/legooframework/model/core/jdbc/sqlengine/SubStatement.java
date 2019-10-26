package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.MoreObjects;

import java.util.Optional;

public class SubStatement {

    private String statement;
    private String resultKey;
    private ResultSetType resultType;

    SubStatement(String resultKey, ResultSetType resultType, String statement) {
        this.resultKey = resultKey;
        this.statement = statement;
        this.resultType = resultType == null ? ResultSetType.LISTMAP : resultType;
    }

    public String getStatement() {
        return statement;
    }

    public Optional<String> getResultKey() {
        return Optional.ofNullable(resultKey);
    }

    public ResultSetType getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("statement", statement)
                .add("resultKey", resultKey)
                .add("resultType", resultType)
                .toString();
    }
}
