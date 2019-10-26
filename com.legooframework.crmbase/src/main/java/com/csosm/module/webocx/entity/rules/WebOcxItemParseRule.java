package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import org.xml.sax.Attributes;

class WebOcxItemParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String type = AttributesUtil.getValue(name, attributes, "type");
        String field = AttributesUtil.getValue(name, attributes, "field");
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String placeholder = AttributesUtil.getIfPresent(attributes, "placeholder").orNull();
        String width = AttributesUtil.getIfPresent(attributes, "width").orNull();
        boolean required = AttributesUtil.getBooleanValue(attributes, "required", false);
        String defvalue = AttributesUtil.getIfPresent(attributes, "defvalue").orNull();
        String dataType = AttributesUtil.getIfPresent(attributes, "dataType").or("string");
        boolean isAll = AttributesUtil.getBooleanValue(attributes, "isAll", false);
        String display = AttributesUtil.getIfPresent(attributes, "display").orNull();
        String position = AttributesUtil.getIfPresent(attributes, "position").orNull();
        WebOcxBuilder item_builder = WebOcxBuilder.createOcxItemBuilder(type, field, _name, placeholder, defvalue,
                required, dataType, isAll,width,display,position);
        getDigester().push(item_builder);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        WebOcxBuilder item_builder = getDigester().pop();
        PageDefinedBuilder pageDefinedBuilder = getDigester().peek();
        pageDefinedBuilder.setOcxItems(item_builder.buildOcxItem());
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/queries/item"};
    }

}
