package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;

public enum CrmRole {

    ADMINISTRATOR(1, "注册人", 99),
    AdminRole(2, "管理员", 97),
    BossRole(3, "老板", 95),
    LeaderRole(4, "经理", 90),
    AreaManagerRole(8, "区域督导", 87),
    ManagerRole(10, "督导", 85),
    StoreManagerRole(5, "店长", 80),
    ShoppingGuideRole(7, "导购", 70),
    StoreMemberRole(11, "会员", 10);

    public static CrmRole parse(int val) {
        CrmRole res;
        switch (val) {
            case 1:
                res = ADMINISTRATOR;
                break;
            case 2:
                res = AdminRole;
                break;
            case 3:
                res = BossRole;
                break;
            case 4:
                res = LeaderRole;
                break;
            case 8:
                res = AreaManagerRole;
                break;
            case 10:
                res = ManagerRole;
                break;
            case 5:
                res = StoreManagerRole;
                break;
            case 7:
                res = ShoppingGuideRole;
                break;
            case 11:
                res = StoreMemberRole;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    private int id;
    private String name;
    private int power;

    CrmRole(int id, String name, int power) {
        this.id = id;
        this.name = name;
        this.power = power;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("power", power)
                .toString();
    }
}
