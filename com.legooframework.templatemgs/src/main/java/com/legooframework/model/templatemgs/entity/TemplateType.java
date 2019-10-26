package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.EnumUtils;

public enum TemplateType {

    GENERAL(1, "通用模板"), COMPANY(2, "公司模板"), STORE(2, "门店模板");

    private final int type;
    private final String desc;

    TemplateType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    static TemplateType paras(String val) {
        return EnumUtils.getEnum(TemplateType.class, val);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("desc", desc)
                .toString();
    }
}
