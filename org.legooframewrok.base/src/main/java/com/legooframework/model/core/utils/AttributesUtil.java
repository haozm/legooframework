package com.legooframework.model.core.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.Optional;

public abstract class AttributesUtil {

    public static Optional<String> getIfPresent(Attributes attributes, String qName) {
        Preconditions.checkNotNull(attributes);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(qName));
        return Optional.ofNullable(Strings.emptyToNull(attributes.getValue(qName)));
    }

    public static String getValue(String nodeName, Attributes attributes, String qName) {
        Optional<String> optional = getIfPresent(attributes, qName);
        Preconditions.checkArgument(optional.isPresent(), "<%s ... %s=... /> can not be empty.",
                nodeName, qName);
        return optional.get();
    }

    public static String[] getValues(String nodeName, Attributes attributes, String qName) {
        Optional<String> optional = getIfPresent(attributes, qName);
        Preconditions.checkArgument(optional.isPresent(), "<%s ... %s=... /> can not be empty.",
                nodeName, qName);
        return StringUtils.split(optional.get(), ',');
    }

    public static Optional<String[]> getValuesIfPresent(Attributes attributes, String qName) {
        Optional<String> optional = getIfPresent(attributes, qName);
        return optional.map(s -> StringUtils.split(s, ','));
    }

    public static Boolean getBooleanValue(Attributes attributes, String qName, boolean devVal) {
        Optional<String> optional = getIfPresent(attributes, qName);
        return optional.map("true"::equals).orElse(devVal);
    }

}
