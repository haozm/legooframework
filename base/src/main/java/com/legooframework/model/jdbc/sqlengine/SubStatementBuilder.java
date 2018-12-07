package com.legooframework.model.jdbc.sqlengine;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Optional;

public class SubStatementBuilder {

    private String statement;
    private String resultKey;
    private ResultSetType resultType;

    public SubStatementBuilder(String resultKey, ResultSetType resultType) {
        this.resultKey = resultKey;
        this.resultType = resultType == null ? ResultSetType.LISTMAP : resultType;
    }

    public void setStatement(String statement) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(statement));
        this.statement = statement;
    }

    public SubStatement building() {
        return new SubStatement(resultKey, resultType, statement);
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
