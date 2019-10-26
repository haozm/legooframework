package com.legooframework.model.statistical.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.statistical.entity.FieldMetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.Optional;

class FieldMetaParseRule extends BaseParseRule {

    private static final Logger logger = LoggerFactory.getLogger(FieldMetaParseRule.class);

    FieldMetaParseRule() {
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String title = AttributesUtil.getValue(name, attributes, "title");
        String id = AttributesUtil.getValue(name, attributes, "id");
        String type = AttributesUtil.getIfPresent(attributes, "type").orElse("str");
        boolean order = AttributesUtil.getBooleanValue(attributes, "order", false);
        Optional<String> fmt = AttributesUtil.getIfPresent(attributes, "fmt");
        Optional<String> drill_type = AttributesUtil.getIfPresent(attributes, "drill_type");
        Optional<String> desc = AttributesUtil.getIfPresent(attributes, "desc");
        FieldMetaEntity fieldMeta = FieldMetaEntity.create(id, title, type, fmt.orElse(null), drill_type.orElse(null),
                order,desc.orElse(null));
        StatisticalEntityBuilder builder = getDigester().peek();
        builder.addFieldMeta(fieldMeta);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Parse FieldMeta %s is OK", fieldMeta));
    }

    @Override
    String[] getPatterns() {
        return new String[]{META_PATH};
    }

}
