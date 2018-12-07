package com.legooframework.model.jdbc.sqlengine.rules;

import org.apache.commons.digester3.Rule;

abstract class BaseParseRule extends Rule {

    public abstract String[] getPatterns();

    protected String getModelName() {
        return getDigester().peek(STK_MODELNAME);
    }

    static String STK_MACROS = "macros";

    static String STK_MODELNAME = "modelName";

}
