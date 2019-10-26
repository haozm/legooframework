package com.legooframework.model.crmadapter.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.crmadapter.entity.TenantsDomainEntity;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

class PostUrlRefParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String _fragment = AttributesUtil.getIfPresent(attributes, "fragment").orElse("true");
        TenantsDomainEntity peek = getDigester().peek();
        peek.addRefConfig(_name, StringUtils.equals("true", _fragment));
    }

    public String[] getPatterns() {
        return new String[]{"domains/domain/url"};
    }
}
