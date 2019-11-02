package com.legooframework.model.core.base.entity;

import com.google.common.base.Charsets;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

public abstract class StringSerializerHelper {

    public static LocalDate decodeLocalDate(String value, LocalDate defult) {
        return StringUtils.equals(StringSerializer.DEF_EMPTY, value) ? defult : DateTimeUtils.parseShortYYYYMMDD(value);
    }

    public static String decodeHex(String value) {
        try {
            return StringUtils.equals(StringSerializer.DEF_EMPTY, value) ? null : new String(Hex.decodeHex(value), Charsets.UTF_8);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static int decodeInt(String value, int defaut) {
        return StringUtils.equals(StringSerializer.DEF_EMPTY, value) ? defaut : Integer.parseInt(value);
    }

    public static Integer decodeInteger(String value, Integer defaut) {
        return StringUtils.equals(StringSerializer.DEF_EMPTY, value) ? defaut : Integer.parseInt(value);
    }

}
