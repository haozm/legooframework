package com.legooframework.model.httpproxy.entity.rules;

import org.apache.commons.digester3.Rule;

abstract class BaseParseRule extends Rule {

    public abstract String[] getPatterns();

    static String KEY_MODULES= "modules";

    static String KEY_MODULE = "modules/module";

}
