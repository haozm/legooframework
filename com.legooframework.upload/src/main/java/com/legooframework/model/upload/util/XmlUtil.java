package com.legooframework.model.upload.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.xml.sax.Attributes;

public abstract class XmlUtil {

    public static Optional<String> getOptValue(Attributes attributes, String qName) {
        return Optional.fromNullable(Strings.emptyToNull(attributes.getValue(qName)));
    }

    public static String getValue(Attributes attributes, String qName) {
        Optional<String> optional =
                Optional.fromNullable(Strings.emptyToNull(attributes.getValue(qName)));
        Preconditions.checkState(optional.isPresent(), "attributes = %s not any value.", qName);
        return optional.get();
    }

    public static String getValue(Attributes attributes, String qName, String devVal) {
        Optional<String> optional = getOptValue(attributes, qName);
        if (!optional.isPresent()) return devVal;
        return optional.get();
    }

    public static Boolean getBooleanValue(Attributes attributes, String qName, boolean devVal) {
        Optional<String> optional = getOptValue(attributes, qName);
        if (!optional.isPresent()) return devVal;
        return "true".equals(optional.get());
    }

    public static Integer getIntValue(Attributes attributes, String qName, Integer devVal) {
        Optional<String> optional = getOptValue(attributes, qName);
        if (!optional.isPresent()) return devVal;
        return NumberUtils.createInteger(optional.get());
    }

    public static Optional<Integer> getOptIntValue(Attributes attributes, String qName) {
        Optional<String> optional = getOptValue(attributes, qName);
        if (!optional.isPresent()) return Optional.absent();
        return Optional.of(NumberUtils.createInteger(optional.get()));
    }
}
