package com.csosm.module.webocx.entity.rules;

import com.csosm.module.webocx.entity.Operate;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import org.xml.sax.Attributes;

class WebOcxButtonsParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        Operate operate = buildOpt(name, attributes);
        PageDefinedBuilder builder = getDigester().peek();
        builder.setButtons(operate);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/batchOperate/operate"};
    }
}
