package com.legooframework.model.membercare.entity;

import com.google.common.base.Enums;

public enum BusinessType {

    BIRTHDAYTOUCH("BIRTHDAYTOUCH", "生日感动"), TOUCHED90("touch90Job", "90服务"), TESTSMS("testSMSJob", "测试短信"),
    BATCHCARE("BATCHCARE", "批量关怀"), COMMONSMS("commonSms", "普通短信");

    private final String desc;
    private final String jobName;

    BusinessType(String jobName, String desc) {
        this.jobName = jobName;
        this.desc = desc;
    }

    public static BusinessType parse(String val) {
        return Enums.stringConverter(BusinessType.class).convert(val);
    }

    public String getDesc() {
        return desc;
    }

    public String getJobName() {
        return jobName;
    }
}
