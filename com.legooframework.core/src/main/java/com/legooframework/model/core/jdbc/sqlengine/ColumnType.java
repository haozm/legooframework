package com.legooframework.model.core.jdbc.sqlengine;

public enum ColumnType {

    PERCENTAGE("百分比"),
    RMB("人名币"),
    MONEY("货币"),
    FLOAT("浮点"),
    STRING("字符"),
    BOOLEAN("布尔值"),
    DATE("日期"),
    DATETIME("日期时间"),
    INT("整数"),
    INTS("整数数组"),
    LONG("长整形"),
    ARRAY("数组"),
    LONGS("LONG数组"),
    RFM("REF值"),
    RANGE("区间"),
    DATERANGE("日期区间");

    private String name;

    ColumnType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
