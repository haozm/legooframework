package com.csosm.module.menu.entity;

import com.google.common.base.MoreObjects;

public enum ResourceType {

    Menu("菜单"), Page("页面"), Button("按钮"), Restful("Web API");

    private String name;

    ResourceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .toString();
    }
}
