package com.csosm.module.menu.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.menu.entity.ResEntity;
import com.csosm.module.menu.entity.ResWebPageEntity;
import com.google.common.collect.ListMultimap;
import org.xml.sax.Attributes;

class WebPageParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        ResWebPageEntity entity = new ResWebPageEntity(AttributesUtil.getValue(name, attributes, "id"),
                AttributesUtil.getValue(name, attributes, "name"),
                AttributesUtil.getValue(name, attributes, "url"),
                AttributesUtil.getIfPresent(attributes, "icon").orNull(),
                AttributesUtil.getIfPresent(attributes, "desc").orNull(),
                getTenantId());
        ResEntity peek = getDigester().peek();
        if (peek != null)
            peek.addSubRes(entity);
        ListMultimap<Long, ResEntity> page_list = getDigester().peek(SEK_PAGE_LIST);
        page_list.put(entity.getTenantId(), entity);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"*/page"};
    }

}
