package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

class WebOcxCdnItemParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String value = AttributesUtil.getValue(name, attributes, "value");
        WebOcxBuilder item_builder = WebOcxBuilder.createCdnItem(_name, value);
        getDigester().push(item_builder);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        WebOcxBuilder item_builder = getDigester().pop();
        PageDefinedBuilder pageDefinedBuilder = getDigester().peek();
        pageDefinedBuilder.setCdnItems(item_builder.buildCdnItem());
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/fixedcdn/item"};
    }

}
