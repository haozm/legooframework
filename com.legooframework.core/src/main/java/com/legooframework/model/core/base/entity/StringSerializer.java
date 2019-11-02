package com.legooframework.model.core.base.entity;

import com.google.common.base.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

public interface StringSerializer<T> {
    /**
     * 序列化
     *
     * @return String
     */
    String serializer();

    /**
     * @param serializer string
     * @return Object
     */
    T deserializer(String serializer);

    String DEF_EMPTY = "NULL";

    default String serializer(Object value) {
        return value == null ? DEF_EMPTY : value.toString();
    }

    default String serializer(boolean value) {
        return value ? "1" : "0";
    }

    default String serializer(LocalDateTime value) {
        return value == null ? DEF_EMPTY : value.toString("yyyyMMddHHmmss");
    }

    default String encodeHex(String value) {
        return value == null ? DEF_EMPTY : Hex.encodeHexString(value.getBytes(Charsets.UTF_8));
    }

    default String decodeHex(String value) {
        try {
            return StringUtils.equals(DEF_EMPTY, value) ? null : new String(Hex.decodeHex(value), Charsets.UTF_8);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }
}
