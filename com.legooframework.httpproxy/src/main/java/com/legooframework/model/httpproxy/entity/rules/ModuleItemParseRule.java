package com.legooframework.model.httpproxy.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.httpproxy.entity.HttpGateWayBuilder;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

class ModuleItemParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        if (StringUtils.equals("target", name)) {
            String domain = AttributesUtil.getValue(name, attributes, "domain");
            String path = AttributesUtil.getValue(name, attributes, "path");
            HttpGateWayBuilder builder = getDigester().peek();
            builder.setPathInfo(domain, path);
        }
    }

    @Override
    public String[] getPatterns() {
        return new String[]{KEY_MODULE + "/target"};
    }

}
