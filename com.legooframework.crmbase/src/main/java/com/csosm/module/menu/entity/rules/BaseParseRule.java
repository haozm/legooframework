package com.csosm.module.menu.entity.rules;

import org.apache.commons.digester3.Rule;

abstract class BaseParseRule extends Rule {

    public abstract String[] getPatterns();

    static String STK_TENANT = "tenant";
    static String SEK_PAGE_LIST = "page_list";

    protected Long getTenantId() {
        return getDigester().peek(STK_TENANT);
    }
}
