package com.csosm.commons.jdbc.sqlcfg.rules;

import org.apache.commons.digester3.Rule;

/**
 * Created by Smart on 2016/5/12
 *
 * @author Smart
 */
public abstract class AbstractSqlParseRule extends Rule {

    public abstract String[] getPatterns();
    
    protected static String KEY_MODEL_NAME ="model";
    
    protected String getModelName() {
        return getDigester().peek(KEY_MODEL_NAME);
    }

}
