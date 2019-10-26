package com.csosm.commons.mvc;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class ServletRequestHelper {

    private static final Logger logger = LoggerFactory.getLogger(ServletRequestHelper.class);

    public static Optional<Map<String, Object>> parseQueryParams(Map<String, String> params) {
        Map<String, Object> paramsMap = Maps.newHashMap();
        if (MapUtils.isEmpty(params)) return Optional.absent();
        String _cursor;
        String _value;
        for (String key : params.keySet()) {
            _cursor = key;
            _value = params.get(key);
            if (StringUtils.startsWith(_cursor, PREFIX_INT)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_INT), null);
                } else {
                    paramsMap.put(
                            StringUtils.removeStart(_cursor, PREFIX_INT), NumberUtils.createInteger(_value));
                }
            } else if (StringUtils.startsWith(_cursor, PREFIX_INTS)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_INTS), Lists.newArrayListWithCapacity(0));
                } else {
                    List<Integer> intList = Lists.newArrayList();
                    List<String> strList = SPLITTER.splitToList(_value);
                    for (String s : strList) intList.add(Integer.valueOf(s));
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_INTS), intList);
                }
            } else if (StringUtils.startsWith(_cursor, PREFIX_STR)) {
                paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_STR), Strings.emptyToNull(_value));
            } else if (StringUtils.startsWith(_cursor, PREFIX_BLN)) {
                paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_BLN),
                        !Strings.isNullOrEmpty(_value) && "true".equalsIgnoreCase(_value));
            } else if (StringUtils.startsWith(_cursor, PREFIX_STRS)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_STRS),
                            Lists.newArrayListWithCapacity(0));
                } else {
                    paramsMap.put(
                            StringUtils.removeStart(_cursor, PREFIX_STRS), SPLITTER.splitToList(_value));
                }
                paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_STR), Strings.emptyToNull(_value));
            } else if (StringUtils.startsWith(_cursor, PREFIX_LONG)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_LONG), null);
                } else {
                    paramsMap.put(
                            StringUtils.removeStart(_cursor, PREFIX_LONG), NumberUtils.createLong(_value));
                }
            } else if (StringUtils.startsWith(_cursor, PREFIX_FLOAT)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_FLOAT), null);
                } else {
                    paramsMap.put(
                            StringUtils.removeStart(_cursor, PREFIX_FLOAT), NumberUtils.createFloat(_value));
                }
            } else if (StringUtils.startsWith(_cursor, PREFIX_DATE)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_DATE), null);
                } else {
                    paramsMap.put(_cursor, DATE_FORMAT.parseDateTime(_value));
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_STR), _value);
                }
            } else if (StringUtils.startsWith(_cursor, PREFIX_TIME)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_TIME), null);
                } else {
                    paramsMap.put(_cursor, TIME_FORMAT.parseDateTime(_value));
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_TIME), _value);
                }
            } else if (StringUtils.startsWith(_cursor, PREFIX_DATETIME)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_DATETIME), null);
                } else {
                    paramsMap.put(_cursor, DATETIME_FORMAT.parseDateTime(_value));
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_DATETIME), _value);
                }
            } else if (StringUtils.startsWith(_cursor, PREFIX_BETWEEN_DATE)) {
                if (StringUtils.isEmpty(_value)) {
                    paramsMap.put(StringUtils.removeStart(_cursor, PREFIX_BETWEEN_DATE), null);
                } else {
                    String _key = StringUtils.removeStart(_cursor, PREFIX_BETWEEN_DATE);
                    BetweenDayDto betweenDateDto = BetweenDayDto.withStartEnd(_value);
                    paramsMap.putAll(betweenDateDto.toDateStr(_key));
                }
            }
        }
        if (logger.isDebugEnabled()) logger.debug(String.format("ParameterMap-%s", paramsMap));
        return Optional.fromNullable(MapUtils.isEmpty(paramsMap) ? null : paramsMap);
    }

    private static final String PREFIX_INT = "int_";
    private static final String PREFIX_BLN = "bln_";
    private static final String PREFIX_INTS = "ints_";
    private static final String PREFIX_STR = "str_";
    //    private static final String PREFIX_LIKE = "lke_";
    private static final String PREFIX_STRS = "strs_";
    private static final String PREFIX_LONG = "long_";
    // private static final String PREFIX_LONGS = "longs_";
    private static final String PREFIX_FLOAT = "float_";
    // private static final String PREFIX_FLOATS = "floats_";
    private static final String PREFIX_DATE = "date_";
    private static final String PREFIX_TIME = "time_";
    private static final String PREFIX_DATETIME = "dateTime_";
    private static final String PREFIX_BETWEEN_DATE = "btn_"; // 日期区间前缀
    private static Splitter SPLITTER = Splitter.on(',');
    private static DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");

}
