package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import org.xml.sax.Attributes;

class WebOcxItemDataParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String type = AttributesUtil.getValue(name, attributes, "type");
        String contxt = AttributesUtil.getIfPresent(attributes, "contxt").orNull();
        WebOcxBuilder ocx_builder = getDigester().peek();
        ocx_builder.setDataSource(type, contxt);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/queries/item/data"};
    }
}
