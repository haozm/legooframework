package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.ColumMeta;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import org.xml.sax.Attributes;

class WebOcxMetaParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String id = AttributesUtil.getValue(name, attributes, "id");
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String type = AttributesUtil.getValue(name, attributes, "type");
        boolean fixed = AttributesUtil.getBooleanValue(attributes, "fixed", false);
        ColumMeta meta = WebOcxBuilder.buildMeta(id, _name, type, fixed);
        PageDefinedBuilder pageDefinedBuilder = getDigester().peek();
        pageDefinedBuilder.setMetas(meta);
    }


    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/meta/col"};
    }
}
