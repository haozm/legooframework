package com.legooframework.model.membercare.entity;

import com.google.common.base.Enums;
import com.legooframework.model.smsprovider.entity.SMSChannel;

public enum BusinessType {

    TESTSMS(0, 2, "测试短信"),
    RIGHTS_AND_INTERESTS_CARE(1, 1, "会员权益提醒"),
    TOUCHED90(6, 1, "90服务关怀"),
    REORDERPLAN(7, 2, "返单计划"),
    TOUCHPLAN(8, 2, "感动计划"),
    BIRTHDAYTOUCH(9, 1, "生日感动"),
    REMINDER_UNKNOWN(10, 2, "忠诚计划"),
    QUICK_APARTMENT(11, 2, "回访计划"),
    CUSTOM_CARE(13, 2, "自定义"),
    AWAKEN(14, 2, "沉睡唤醒"),
    BATCH_GENERALCARE(17, 2, "批量普通关怀"),
    BATCH_BIRTHDAYCARE(18, 1, "批量生日关怀"),
    BATCH_HOLIDAYCARE(19, 2, "批量节日关怀"),
    HOLIDAYCARE(21, 2, "节日关怀"),
    FAMILYHOLIDAYCARE(22, 2, "家庭节日关怀");

    public static BusinessType paras(int val) {
        BusinessType res;
        switch (val) {
            case 1:
                res = RIGHTS_AND_INTERESTS_CARE;
                break;
            case 22:
                res = FAMILYHOLIDAYCARE;
                break;
            case 19:
                res = BATCH_HOLIDAYCARE;
                break;
            case 18:
                res = BATCH_BIRTHDAYCARE;
                break;
            case 0:
                res = TESTSMS;
                break;
            case 7:
                res = REORDERPLAN;
                break;
            case 6:
                res = TOUCHED90;
                break;
            case 8:
                res = TOUCHPLAN;
                break;
            case 9:
                res = BIRTHDAYTOUCH;
                break;
            case 10:
                res = REMINDER_UNKNOWN;
                break;
            case 11:
                res = QUICK_APARTMENT;
                break;
            case 13:
                res = CUSTOM_CARE;
                break;
            case 14:
                res = AWAKEN;
                break;
            case 17:
                res = BATCH_GENERALCARE;
                break;
            case 21:
                res = HOLIDAYCARE;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参....%d", val));
        }
        return res;
    }

    private final String desc;
    private final int type, smsChannel;

    public SMSChannel getSMSChannel() {
        return SMSChannel.paras(this.smsChannel);
    }

    BusinessType(int type, int smsChannel, String desc) {
        this.type = type;
        this.desc = desc;
        this.smsChannel = smsChannel;
    }

    public int getSmsChannel() {
        return smsChannel;
    }

    public static BusinessType parse(String val) {
        return Enums.stringConverter(BusinessType.class).convert(val);
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }
}
