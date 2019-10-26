package com.legooframework.model.statistical.entity.rules;

import com.legooframework.model.core.utils.AttributesUtil;
import com.legooframework.model.statistical.entity.FieldMetaRefEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.Optional;

class FieldMetaRefParseRule extends BaseParseRule {

    private static final Logger logger = LoggerFactory.getLogger(FieldMetaRefParseRule.class);

    FieldMetaRefParseRule() {
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String refId = AttributesUtil.getValue(name, attributes, "refId");
        Optional<String> serieType = AttributesUtil.getIfPresent(attributes, "serieType");
        Optional<String> axisY = AttributesUtil.getIfPresent(attributes, "axisY");
        Optional<String> group = AttributesUtil.getIfPresent(attributes, "group");
        boolean primary = AttributesUtil.getBooleanValue(attributes, "primary", false);
        boolean drill = AttributesUtil.getBooleanValue(attributes, "drill", false);
        boolean order = AttributesUtil.getBooleanValue(attributes, "order", false);
        FieldMetaRefEntity fieldMetaRef = new FieldMetaRefEntity(refId, drill, order, primary, serieType.orElse(null),
                axisY.orElse(null), group.orElse(null));
        FieldMetaRefSupport builder = getDigester().peek();
        builder.addFieldMetaRef(fieldMetaRef);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Parse FieldMetaRefEntity %s is OK", fieldMetaRef));
    }

    @Override
    String[] getPatterns() {
        return new String[]{TABLE_HEADER_FIELD_PATH, SUMMARY_FIELD_PATH, SUBSUMMARY_FIELD_PATH, ECHART_FIELD_PATH};
    }

}
