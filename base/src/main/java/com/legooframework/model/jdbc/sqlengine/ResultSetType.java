package com.legooframework.model.jdbc.sqlengine;

public enum ResultSetType {

    OBJECT("单值"), LIST("单值LIST"), MAP("单条记录"), LISTMAP("多条记录");

    private String name;

    ResultSetType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
