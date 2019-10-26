package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;

public enum PermissionType {

    PERMITED(0, "开放"), THREEDAYS(2004, "仅三天"), MONTHDAYS(2005, "仅一个月"), HALFOFYEAR(2001, "仅半年"),
    UNKNOWN(99, "未知类型");

    private final int permission;
    private final String desc;

    PermissionType(int permission, String desc) {
        this.permission = permission;
        this.desc = desc;
    }

    public static PermissionType parse(int permission) {
        switch (permission) {
            case 0:
                return PermissionType.PERMITED;
            case 2004:
                return PermissionType.THREEDAYS;
            case 2005:
                return PermissionType.MONTHDAYS;
            case 2001:
                return PermissionType.HALFOFYEAR;
            default:
                return PermissionType.UNKNOWN;
        }
    }

    int getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("permission", permission)
                .add("desc", desc)
                .toString();
    }
}
