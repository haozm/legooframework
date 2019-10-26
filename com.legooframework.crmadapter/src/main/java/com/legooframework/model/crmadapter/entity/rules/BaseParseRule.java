package com.legooframework.model.crmadapter.entity.rules;

import org.apache.commons.digester3.Rule;

abstract class BaseParseRule extends Rule {

    public abstract String[] getPatterns();

}
