package com.csosm.module.webocx.entity.rules;

import com.csosm.commons.util.AttributesUtil;
import com.csosm.module.webocx.entity.PageDefined;
import com.csosm.module.webocx.entity.PageDefinedBuilder;
import com.csosm.module.webocx.entity.WebOcxBuilder;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.Set;

class WebOcxGroupParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String company = AttributesUtil.getValue(name, attributes, "company");
        String subId = AttributesUtil.getIfPresent(attributes, "id").or("sub001");
        Integer companyId = StringUtils.equals("*", company) ? -1 : Integer.valueOf(company);
        Optional<String> store = AttributesUtil.getIfPresent(attributes, "stores");
        Set<Integer> storeIds = null;
        if (store.isPresent()) {
            storeIds = Sets.newHashSet();
            for (String $it : StringUtils.split(store.get(), ',')) {
                storeIds.add(Integer.valueOf($it));
            }
        }
        String title = AttributesUtil.getIfPresent(attributes, "title").orNull();
        String desc = AttributesUtil.getIfPresent(attributes, "desc").orNull();
        PageDefinedBuilder builder = new PageDefinedBuilder(subId, companyId, storeIds, title, desc);
        getDigester().push(builder);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        PageDefinedBuilder pageDefinedBuilder = getDigester().pop();
        PageDefined pageDefined = pageDefinedBuilder.building();
        WebOcxBuilder webOcxBuilder = getDigester().peek();
        webOcxBuilder.setGroup(pageDefined);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"webocxs/webocx/group"};
    }
}
