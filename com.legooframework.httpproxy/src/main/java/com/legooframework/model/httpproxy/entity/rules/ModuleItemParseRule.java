package com.legooframework.model.httpproxy.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

class ModuleItemParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        if (StringUtils.equals("target", name)) {
            String target = AttributesUtil.getValue(name, attributes, "value");
            HttpGateWayBuilder builder = getDigester().peek(KEY_MODULE);
            builder.setTarget(target);
        }
    }


    @Override
    public String[] getPatterns() {
        return new String[]{KEY_MODULE + "/**"};
    }

}
