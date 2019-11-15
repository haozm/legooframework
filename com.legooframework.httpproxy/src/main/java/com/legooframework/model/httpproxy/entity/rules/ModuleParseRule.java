package com.legooframework.model.httpproxy.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.httpproxy.entity.HttpGateWayBuilder;
import com.legooframework.model.httpproxy.entity.HttpGateWayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.List;

class ModuleParseRule extends BaseParseRule {

    private static final Logger logger = LoggerFactory.getLogger(ModuleParseRule.class);

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String params = AttributesUtil.getValue(name, attributes, "params");
        String id = AttributesUtil.getValue(name, attributes, "id");
        String fuse = AttributesUtil.getValue(name, attributes, "fuse");
        String timeout = AttributesUtil.getIfPresent(attributes, "timeout").orElse("60");
        HttpGateWayBuilder builder = new HttpGateWayBuilder(id, params, Integer.parseInt(fuse), Integer.parseInt(timeout));
        getDigester().push(builder);
    }

    @Override
    public void body(String namespace, String name, String text) throws Exception {
        HttpGateWayBuilder builder = getDigester().pop();
        HttpGateWayEntity item = builder.build();
        if (logger.isDebugEnabled())
            logger.debug("BULDER ITEM:" + item.toString());
        List<HttpGateWayEntity> items = getDigester().peek();
        items.add(item);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{KEY_MODULE};
    }
}
