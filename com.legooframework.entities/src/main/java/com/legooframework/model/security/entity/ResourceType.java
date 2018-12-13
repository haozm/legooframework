package com.legooframework.model.security.entity;

public enum ResourceType {

    Menu("菜单"), Page("页面"), Button("按钮"), Restful("Web API");

    private String name;

    ResourceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
