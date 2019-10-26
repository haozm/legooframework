package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;

public class SimpleMsgTemplate {

    private final Integer companyId, storeId;
    private String classifies, template;
    private boolean isDefault;

    private SimpleMsgTemplate(String payload) {
        String[] args = StringUtils.split(payload, '|');
        Preconditions.checkState(args.length == 6, "非法的入参%s，无法转化为SimpleMsgTemplate", payload);
        this.companyId = Integer.valueOf(args[0]);
        this.storeId = Integer.valueOf(args[1]);
        this.classifies = args[2];
        this.isDefault = StringUtils.equals("1", args[3]);
        boolean encoding = StringUtils.equals("1", args[4]);
        this.template = encoding ? WebUtils.decodeUrl(args[5]) : args[5];
    }

    public String getTemplate() {
        return template;
    }

    static SimpleMsgTemplate create(String payload) {
        return new SimpleMsgTemplate(payload);
    }

    boolean isClassifies(String classifies) {
        return StringUtils.equals(this.classifies, classifies);
    }

    boolean isCompany() {
        return storeId == -1;
    }

    boolean hasStore(Integer storeId) {
        return this.storeId.equals(storeId);
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("classifies", classifies)
                .add("isDefault", isDefault)
                .add("template", template)
                .toString();
    }
}
