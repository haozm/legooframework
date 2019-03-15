package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import com.csosm.module.webocx.entity.SubPageDefined;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

class WebOcxSubPageItemParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        boolean paged = AttributesUtil.getBooleanValue(attributes, "paged", false);
        String id = AttributesUtil.getValue(name, attributes, "id");
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String keys = AttributesUtil.getValue(name, attributes, "keys");
        String[] args_key = StringUtils.split(keys, ',');
        String stmtId = AttributesUtil.getValue(name, attributes, "stmtId");
        String url = AttributesUtil.getValue(name, attributes, "url");
        SubPageDefined subPageDefined = PageDefinedBuilder.buildSubPage(paged, id, _name, stmtId, url, args_key);
        PageDefinedBuilder pageDefinedBuilder = getDigester().peek();
        pageDefinedBuilder.setSubPageDefineds(subPageDefined);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group/subpage/table"};
    }
}
