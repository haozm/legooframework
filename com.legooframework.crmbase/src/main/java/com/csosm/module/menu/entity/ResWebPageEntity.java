package com.csosm.module.menu.entity;

public class ResWebPageEntity extends ResWebEntity {

    public ResWebPageEntity(String id, String name, String url, String icon, String desc, Long tenantId) {
        super(id, ResourceType.Page, name, desc, url, icon, tenantId);
    }

}
