package com.csosm.module.webocx.entity.rules;

import java.util.List;
import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import com.csosm.module.webocx.entity.SubPageDefined;
import com.google.common.base.Preconditions;
import org.xml.sax.Attributes;

class WebOcxSubPageParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String active = AttributesUtil.getValue(name, attributes, "active");
        Preconditions.checkNotNull(active);
        PageDefinedBuilder pageDefinedBuilder = getDigester().peek();
        pageDefinedBuilder.setActive(active);
    }


    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/subpage"};
    }
}
