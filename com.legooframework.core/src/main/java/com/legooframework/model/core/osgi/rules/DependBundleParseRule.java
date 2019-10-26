package com.legooframework.model.core.osgi.rules;

import com.legooframework.model.core.osgi.DefaultBundleBuilder;
import com.legooframework.model.core.utils.AttributesUtil;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.Optional;

class DependBundleParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String model_name = AttributesUtil.getValue(name, attributes, "name");
        Optional<String> model_version = AttributesUtil.getIfPresent(attributes, "version");
        Optional<String> use_type = AttributesUtil.getIfPresent(attributes, "use");
        DefaultBundleBuilder builder = getDigester().peek();
        builder.setDependsItems(model_name, model_version.orElse("1.0.0"),
                use_type.isPresent() && StringUtils.equals("required", use_type.get()));
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"model/depends/bundle"};
    }
}
