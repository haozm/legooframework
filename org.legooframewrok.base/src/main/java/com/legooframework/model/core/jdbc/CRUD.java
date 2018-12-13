package com.legooframework.model.core.jdbc;

public enum CRUD {

    C(1, "创建"), R(2, "读取"), U(0, "修改"), D(-1, "删除");

    private final int value;
    private final String desc;

    static CRUD valueOf(int val) {
        CRUD res;
        switch (val) {
            case 1:
                res = C;
                break;
            case 2:
                res = R;
                break;
            case 0:
                res = U;
                break;
            case -1:
                res = D;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }


    CRUD(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
