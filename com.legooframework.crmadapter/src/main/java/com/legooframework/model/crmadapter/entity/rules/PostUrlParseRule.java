package com.legooframework.model.crmadapter.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.crmadapter.entity.TenantsRouteFactory;
import com.legooframework.model.crmadapter.entity.TenantsRouteFactoryBuilder;
import org.xml.sax.Attributes;

import java.util.List;

class PostUrlParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        TenantsRouteFactoryBuilder builder = getDigester().peek();
        String _name = AttributesUtil.getValue(name, attributes, "name");
        String _value = AttributesUtil.getValue(name, attributes, "value");
        String _fragment = AttributesUtil.getIfPresent(attributes, "fragment").orElse(null);
        TenantsRouteFactory.UrlItem urlItem = TenantsRouteFactory.createUrlItem(_name, _fragment, _value);
        builder.addUrlItem(urlItem);
    }

    public String[] getPatterns() {
        return new String[]{"domains/urls/url"};
    }

}
