package com.legooframework.model.httpproxy.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import org.xml.sax.Attributes;

class ModuleParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String ant_path = AttributesUtil.getValue(name, attributes, "ant");
        String id = AttributesUtil.getValue(name, attributes, "id");
        HttpGateWayBuilder builder = new HttpGateWayBuilder(id, ant_path);
        getDigester().push(KEY_MODULE, builder);
    }

    @Override
    public void body(String namespace, String name, String text) throws Exception {

    }

    @Override
    public String[] getPatterns() {
        return new String[]{KEY_MODULE};
    }
}
