package com.csosm.commons.jdbc.sqlcfg;

import com.csosm.commons.util.RegexUtil;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class QueryParam {

    private final String name, type;
    private final boolean required;
    private final Object value;
    private final String fmt;

    QueryParam(String name, String type, boolean required, Object value, String fmt) {
        this.name = name;
        this.type = type;
        this.fmt = Strings.emptyToNull(fmt);
        this.required = required;
        this.value = value;
    }

    public boolean isFmt4Like() {
        return !Strings.isNullOrEmpty(fmt) && StringUtils.equalsIgnoreCase("like", fmt);
    }

    public static QueryParam createStrParam(String name, boolean required, String value, String fmt) {
        return new QueryParam(name, "str", required, value, fmt);
    }

    public static QueryParam createStrsParam(String name, boolean required, String value, String fmt) {
        List<String> int_list = Lists.newArrayListWithCapacity(0);
        return new QueryParam(name, "strs", required, int_list, fmt);
    }

    public static QueryParam createIntParam(String name, boolean required, String value, String fmt) {
        Integer def_value = Strings.isNullOrEmpty(value) ? null : Integer.valueOf(value);
        return new QueryParam(name, "int", required, def_value, fmt);
    }

    public static QueryParam createLongParam(String name, boolean required, String value, String fmt) {
        Integer def_value = Strings.isNullOrEmpty(value) ? null : Integer.valueOf(value);
        return new QueryParam(name, "long", required, def_value, fmt);
    }

    public static QueryParam createLongsParam(String name, boolean required, String value, String fmt) {
        Integer def_value = Strings.isNullOrEmpty(value) ? null : Integer.valueOf(value);
        return new QueryParam(name, "longs", required, def_value, fmt);
    }

    public static QueryParam createIntsParam(String name, boolean required, String value, String fmt) {
        List<Integer> int_list = Lists.newArrayListWithCapacity(0);
        return new QueryParam(name, "ints", required, int_list, fmt);
    }

    public static QueryParam createBooleanParam(String name, boolean required, String value, String fmt) {
        Boolean def_value = StringUtils.equals("true", value);
        return new QueryParam(name, "bln", required, def_value, fmt);
    }

    public static QueryParam createBirthdayRangeParam(String name, boolean required, String value, String fmt) {
        return new QueryParam(name, "birthdayRange", required, null, fmt);
    }


    public static QueryParam createBetweenParam(String name, boolean required, String value, String fmt) {
        return new QueryParam(name, "btn", required, null, fmt);
    }

    void handleParams(Map<String, Object> dataModel) {
        if (isRequired()) {
            Preconditions.checkArgument(MapUtils.isNotEmpty(dataModel) &&
                    MapUtils.getObject(dataModel, name) != null, "缺少必要的查询参数 %s", name);
        }
        Object obj = MapUtils.getObject(dataModel, name);
        if (obj == null) return;
        if (isStr()) {
            if (isFmt4Like()) {
                String value = MapUtils.getString(dataModel, name);
                // 如果已经符合输出 则忽略
                if (RegexUtil.likePattern(value)) return;
                String fmt = String.format("%%%s%%", value);
                dataModel.put(name, fmt);
            }
        }
        if (isStrs()) {
            if (obj instanceof String[]) return;
            String _temp = (String) obj;
            dataModel.put(name, StringUtils.split(_temp, ','));
        } else if (isInt()) {
            if (obj instanceof Integer) return;
            dataModel.put(name, MapUtils.getIntValue(dataModel, name));
        } else if (isLong()) {
            if (obj instanceof Long) return;
            dataModel.put(name, MapUtils.getLong(dataModel, name));
        } else if (isInts()) {
            if (obj instanceof Integer[]) return;
            String _temp = (String) obj;
            String[] strs = StringUtils.split(_temp, ',');
            List<Integer> list = Lists.newArrayList();
            for (String $it : strs) list.add(Integer.valueOf($it));
            dataModel.put(name, list.toArray(new Integer[]{}));
        } else if (isLongs()) {
            if (obj instanceof Long[]) return;
            String _temp = (String) obj;
            String[] strs = StringUtils.split(_temp, ',');
            List<Long> list = Lists.newArrayList();
            for (String $it : strs) list.add(Long.valueOf($it));
            dataModel.put(name, list.toArray(new Long[]{}));
        } else if (isBln()) {
            if (obj instanceof Boolean) return;
            dataModel.put(name, MapUtils.getBoolean(dataModel, name, false));
        } else if (isBtn()) {
            if (obj instanceof String[]) return;
            String _temp = (String) obj;
            dataModel.put(name, StringUtils.split(_temp, ','));
        } else if (isBirthdayRange()) {
            if (obj instanceof String[]) return;
            String[] _tem = StringUtils.split((String) obj, ',');
            dataModel.put(name, new String[]{_tem[0].substring(5), _tem[1].substring(5)});
        }
    }

    public boolean exitsFmt() {
        return !Strings.isNullOrEmpty(fmt);
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public Optional<Object> getValue() {
        return Optional.fromNullable(value);
    }

    public boolean isBirthdayRange() {
        return "birthdayRange".equals(this.type);
    }

    public boolean isStr() {
        return "str".equals(this.type);
    }

    public boolean isStrs() {
        return "strs".equals(this.type);
    }

    public boolean isInt() {
        return "int".equals(this.type);
    }

    public boolean isInts() {
        return "ints".equals(this.type);
    }

    public boolean isLongs() {
        return "longs".equals(this.type);
    }

    public boolean isBln() {
        return "bln".equals(this.type);
    }

    public boolean isBtn() {
        return "btn".equals(this.type);
    }

    public boolean isLong() {
        return "long".equals(this.type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("type", type)
                .add("required", required)
                .add("value", value)
                .add("fmt", fmt)
                .toString();
    }
}
