package com.csosm.module.menu.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.menu.entity.ResEntity;
import com.csosm.module.menu.entity.ResMenuEntity;
import org.xml.sax.Attributes;

class MenuParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        ResMenuEntity entity = new ResMenuEntity(AttributesUtil.getValue(name, attributes, "id"),
                AttributesUtil.getValue(name, attributes, "name"),
                AttributesUtil.getIfPresent(attributes, "desc").orNull(),
                getTenantId());

        ResEntity peek = getDigester().peek();
        if (peek != null)
            peek.addSubRes(entity);

        getDigester().push(entity);

    }

    @Override
    public void end(String namespace, String name) throws Exception {
        getDigester().pop();
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"*/menu"};
    }

}
