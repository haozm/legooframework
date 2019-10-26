package com.legooframework.model.scheduler.entity;

import com.google.common.base.MoreObjects;

public enum TriggerType {

    CronTrigger(1, "CronTrigger"), SimpleTrigger(2, "SimpleTrigger");

    private final int type;
    private final String desc;

    public static TriggerType parse(int type) {
        switch (type) {
            case 1:
                return TriggerType.CronTrigger;
            case 2:
                return TriggerType.SimpleTrigger;
            default:
                throw new IllegalArgumentException(String.format("非法参数%s，无法转化为 TriggerType", type));
        }
    }


    TriggerType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("desc", desc)
                .toString();
    }
}
