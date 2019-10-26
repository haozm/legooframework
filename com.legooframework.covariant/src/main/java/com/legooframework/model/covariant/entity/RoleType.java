package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;

public enum RoleType {
    Member(11, "Member", "会员"),
    ShoppingGuide(7, "ShoppingGuide", "导购"),
    StoreManager(5, "StoreManager", "店长"),
    Manager(4, "Manager", "督导"),
    Boss(3, "Boss", "老板"),
    Admin(2, "Admin", "系统管理员"),
    SuperMan(1, "SuperMan", "超级管理员");

    public static RoleType[] get4Company() {
        return new RoleType[]{Member, ShoppingGuide, StoreManager, Manager, Boss, Admin};
    }

    public static RoleType[] get4Store() {
        return new RoleType[]{Member, ShoppingGuide, StoreManager};
    }

    private int value;
    private String desc, name;

    public static RoleType parasStr(String name) {
        RoleType res;
        switch (name) {
            case "SuperMan":
                res = SuperMan;
                break;
            case "Admin":
                res = Admin;
                break;
            case "Boss":
                res = Boss;
                break;
            case "Manager":
                res = Manager;
                break;
            case "ShoppingGuide":
                res = ShoppingGuide;
                break;
            case "StoreManager":
                res = StoreManager;
                break;
            case "Member":
                res = Member;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参....%s", name));
        }
        return res;
    }

    public static RoleType paras(int val) {
        RoleType res;
        switch (val) {
            case 12:
                res = SuperMan;
                break;
            case 2:
                res = Admin;
                break;
            case 3:
                res = Boss;
                break;
            case 4:
                res = Manager;
                break;
            case 7:
                res = ShoppingGuide;
                break;
            case 5:
                res = StoreManager;
                break;
            case 11:
                res = Member;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参....%d", val));
        }
        return res;
    }

    RoleType(int value, String name, String desc) {
        this.value = value;
        this.name = name;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("name", name)
                .add("desc", desc)
                .toString();
    }
}
