package com.csosm.module.menu.entity;

public class ResMenuEntity extends ResEntity {

    public ResMenuEntity(String id, String name, String desc, Long tenantId) {
        super(id, ResourceType.Menu, name, desc, tenantId);
    }

    public static ResMenuEntity createRoot(String desc, Long tenantId) {
        ResMenuEntity root = new ResMenuEntity("root", "根目录", desc, tenantId);
        root.setPaths(null);
        return root;
    }

}
