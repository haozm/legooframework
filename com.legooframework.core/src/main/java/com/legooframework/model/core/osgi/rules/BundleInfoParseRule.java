package com.legooframework.model.core.osgi.rules;

import com.legooframework.model.core.osgi.DefaultBundleBuilder;
import com.legooframework.model.core.utils.AttributesUtil;
import org.xml.sax.Attributes;

import java.util.Optional;

class BundleInfoParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String model_name = AttributesUtil.getValue(name, attributes, "name");
        Optional<String> model_version = AttributesUtil.getIfPresent(attributes, "version");
        DefaultBundleBuilder builder = new DefaultBundleBuilder(model_name, model_version.orElse("1.0.0"));
        getDigester().push(builder);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"model"};
    }
}
