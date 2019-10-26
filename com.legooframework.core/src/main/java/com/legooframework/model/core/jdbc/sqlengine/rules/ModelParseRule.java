package com.legooframework.model.core.jdbc.sqlengine.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import org.xml.sax.Attributes;

class ModelParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String model_name = AttributesUtil.getValue(name, attributes, "id");
        getDigester().push(STK_MODELNAME, model_name);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        getDigester().pop(STK_MODELNAME);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"sqls/model"};
    }
}
