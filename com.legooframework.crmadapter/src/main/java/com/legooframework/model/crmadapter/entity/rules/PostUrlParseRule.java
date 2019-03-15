package com.legooframework.model.crmadapter.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.crmadapter.entity.TenantsRouteBuilder;
import org.xml.sax.Attributes;

class PostUrlParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String _value = AttributesUtil.getValue(name, attributes, "value");
        TenantsRouteBuilder builder = getDigester().peek();
        builder.setPostUrls(_name, _value);
    }

    public String[] getPatterns() {
        return new String[]{"tenants/tenant/url"};
    }

}
