package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import com.google.common.base.Optional;
import org.xml.sax.Attributes;

class WebOcxItemDataItemParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String label = AttributesUtil.getValue(name, attributes, "label");
        String value = AttributesUtil.getValue(name, attributes, "value");
        Optional<String> checked = AttributesUtil.getIfPresent(attributes, "checked");
        WebOcxBuilder builder = getDigester().peek();
        builder.setDatas(label, value, checked.orNull());
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/queries/item/data/item"};
    }
}
