package com.legooframework.model.core.jdbc.sqlengine;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParamMeta {
    private static final Logger logger = LoggerFactory.getLogger(ParamMeta.class);
    private final String name;
    private final ColumnType type;
    private final boolean required;
    private final Object value;
    private final String format;

    public ParamMeta(String name, ColumnType type, boolean required, Object value, String format) {
        this.name = name;
        this.type = type;
        this.format = Strings.emptyToNull(format);
        this.required = required;
        this.value = value;
    }

    void handleParams(Map<String, Object> queryParams) {
        if (required) {
            Preconditions.checkState(MapUtils.isNotEmpty(queryParams), "执行SQL缺少必要入参 %s", name);
            Preconditions.checkState(queryParams.containsKey(name), "缺少SQL执行必须参数: %s", name);
            Preconditions.checkNotNull(MapUtils.getObject(queryParams, name), "缺少SQL执行必须参数: %s", name);
        }
        // 默认 仅支持 字符串
        Object _val = MapUtils.getObject(queryParams, name);
        if (null == _val) {
            queryParams.remove(name);
            if (logger.isTraceEnabled())
                logger.debug(String.format("handleParams ( %s ) is null,so remove it and return ", name));
            return;
        }
        if (isString()) {
            if (_val instanceof String && !Strings.isNullOrEmpty(format)) {
                boolean formated = MapUtils.getBoolean(queryParams, String.format("%s_fmted", name), false);
                if (formated) return; // 防止多次处理
                queryParams.put(name, StringUtils.replace(format, "{value}", (String) _val));
                if (logger.isTraceEnabled())
                    logger.debug(String.format("handleParams fmt( %s -> %s ) ", (String) _val, queryParams.get(name)));
                queryParams.put(String.format("%s_fmted", name), true);
            }
        } else if (isArray()) { // 字符串数组
            String split_tag = Strings.isNullOrEmpty(this.format) ? "," : this.format;
            if (_val instanceof String) {
                queryParams.put(name, StringUtils.split((String) _val, split_tag));
                if (logger.isTraceEnabled())
                    logger.debug(String.format("handleParams Array( %s -> args[%s] ) ", _val,
                            Arrays.toString((String[]) queryParams.get(name))));
            }
        } else if (isLong()) {
            if (_val instanceof Long) return;
            if (_val instanceof String) queryParams.put(name, NumberUtils.createLong(_val.toString()));
        } else if (isInt()) {
            if (_val instanceof Integer) return;
            if (_val instanceof String) queryParams.put(name, NumberUtils.createInteger(_val.toString()));
        } else if (isInts()) {
            if (_val instanceof Integer[] || _val instanceof Long[] || _val instanceof List || _val instanceof Set)
                return;
            if (_val instanceof String) {
                String[] vals = StringUtils.split((String) _val, ',');
                List<Integer> lgs = Stream.of(vals).map(Integer::valueOf).collect(Collectors.toList());
                queryParams.put(name, lgs);
            }
        } else if (isLongs()) {
            if (_val instanceof Long[] || _val instanceof List || _val instanceof Set) return;
            if (_val instanceof String) {
                String[] vals = StringUtils.split((String) _val, ',');
                List<Long> lgs = Stream.of(vals).map(Long::valueOf).collect(Collectors.toList());
                queryParams.put(name, lgs);
            }
        } else if (isBoolean()) {
            if (_val instanceof Boolean) return;
            queryParams.put(name, StringUtils.equalsIgnoreCase("true", _val.toString()));
        } else if (isRfm()) {
            boolean formated = MapUtils.getBoolean(queryParams, String.format("%s_fmted", name), false);
            if (formated) return; // 防止多次处理
            // minR,maxR,minF,maxF,minM,maxM  特殊函数的处理机制
            Object raw_val = MapUtils.getObject(queryParams, name);
            if (raw_val instanceof String) {
                String[] rmf_args = StringUtils.split((String) raw_val, ',');
                Preconditions.checkArgument(ArrayUtils.isNotEmpty(rmf_args) && rmf_args.length == 6,
                        "非法的RFM值 %s", raw_val);
                queryParams.put(String.format("%s_vs", name), rmf_args);
                queryParams.put(String.format("%s_fmted", name), true);
                if (logger.isTraceEnabled())
                    logger.debug(String.format("handleParams RFM( %s -> args[%s] ) ", raw_val, Arrays.toString(rmf_args)));
            }
        } else if (isDateRange()) {
            boolean formated = MapUtils.getBoolean(queryParams, String.format("%s_fmted", name), false);
            if (formated) return; // 防止多次处理
            Object raw_val = MapUtils.getObject(queryParams, name);
            if (raw_val instanceof String) {
                queryParams.put(name, true);
                String[] val_args = StringUtils.split((String) raw_val, ',');
                Preconditions.checkArgument(ArrayUtils.isNotEmpty(val_args) && val_args.length == 2,
                        "非法的日期范围解析值 %s", raw_val);
                queryParams.put(String.format("%s_start", name), val_args[0]);
                queryParams.put(String.format("%s_end", name), val_args[1]);
                queryParams.put(String.format("%s_fmted", name), true);
            }
        } else if (isRange()) {
            boolean formated = MapUtils.getBoolean(queryParams, String.format("%s_fmted", name), false);
            if (formated) return; // 防止多次处理
            Object raw_val = MapUtils.getObject(queryParams, name);
            if (raw_val instanceof String) {
                queryParams.put(name, true);
                String[] val_args = StringUtils.split((String) raw_val, ',');
                Preconditions.checkArgument(ArrayUtils.isNotEmpty(val_args) && val_args.length == 2,
                        "非法的区间范围解析值 %s", raw_val);
                queryParams.put(String.format("%s_min", name), val_args[0]);
                queryParams.put(String.format("%s_max", name), val_args[1]);
                queryParams.put(String.format("%s_fmted", name), true);
            }
        }
    }

    boolean isString() {
        return ColumnType.STRING == this.type;
    }

    private boolean isArray() {
        return ColumnType.ARRAY == this.type;
    }

    private boolean isInts() {
        return ColumnType.INTS == this.type;
    }

    boolean isLong() {
        return ColumnType.LONG == this.type;
    }

    boolean isBoolean() {
        return ColumnType.BOOLEAN == this.type;
    }

    private boolean isLongs() {
        return ColumnType.LONGS == this.type;
    }

    boolean isInt() {
        return ColumnType.INT == this.type;
    }

    private boolean isRfm() {
        return ColumnType.RFM == this.type;
    }

    private boolean isDateRange() {
        return ColumnType.DATERANGE == this.type;
    }

    private boolean isRange() {
        return ColumnType.RANGE == this.type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("type", type)
                .add("required", required)
                .add("value", value)
                .add("format", format)
                .omitNullValues()
                .toString();
    }
}
