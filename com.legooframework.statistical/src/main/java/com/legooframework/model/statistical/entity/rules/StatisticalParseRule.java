package com.legooframework.model.statistical.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import org.xml.sax.Attributes;

import java.util.List;

class StatisticalParseRule extends BaseParseRule {

    StatisticalParseRule() {
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String id = AttributesUtil.getValue(name, attributes, "id");
        String title = AttributesUtil.getValue(name, attributes, "title");
        StatisticalEntityBuilder builder = new StatisticalEntityBuilder(id, title);
        getDigester().push(builder);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        StatisticalEntityBuilder builder = getDigester().pop();
        List<StatisticalEntityBuilder> builders = getDigester().peek();
        builders.add(builder);
    }

    @Override
    String[] getPatterns() {
        return new String[]{ROOT_PATH};
    }
}
