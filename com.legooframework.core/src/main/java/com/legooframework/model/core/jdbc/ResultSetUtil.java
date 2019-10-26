package com.legooframework.model.core.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class ResultSetUtil {

    private static Splitter spilt = Splitter.on(',');

    public static String getOptString(ResultSet res, String columnLabel, String defVal) throws SQLException {
        String value = res.getString(columnLabel);
        return Strings.isNullOrEmpty(value) ? defVal : value;
    }

    public static String getString(ResultSet res, String columnLabel) throws SQLException {
        String value = Strings.emptyToNull(res.getString(columnLabel));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(value),
                "column = %s 对应的String值不可以为空", columnLabel);
        return value;
    }

    public static boolean getBooleanByInt(ResultSet res, String columnLabel) throws SQLException {
        int value = res.getInt(columnLabel);
        return value >= 1;
    }

    public static Optional<List<String>> getStrList(ResultSet res, String columnLabel) throws SQLException {
        String value = res.getString(columnLabel);
        if (Strings.isNullOrEmpty(value)) return Optional.empty();
        return Optional.of(Lists.newArrayList(spilt.split(value)));
    }

    public static Optional<Set<String>> getStrSet(ResultSet res, String columnLabel) throws SQLException {
        String value = res.getString(columnLabel);
        if (Strings.isNullOrEmpty(value)) return Optional.empty();
        return Optional.of(Sets.newHashSet(spilt.split(value)));
    }

    public static DateTime getDateTime(ResultSet res, String columnLabel) throws SQLException {
        Date value = res.getDate(columnLabel);
        return value == null ? null : new DateTime(value);
    }

    public static LocalDate getLocalDate(ResultSet res, String columnLabel) throws SQLException {
        Date value = res.getDate(columnLabel);
        return value == null ? null : LocalDate.fromDateFields(value);
    }

    public static LocalDateTime getLocalDateTime(ResultSet res, String columnLabel) throws SQLException {
        Timestamp value = res.getTimestamp(columnLabel);
        return value == null ? null : LocalDateTime.fromDateFields(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getOptObject(ResultSet res, String columnLabel, Class<T> clazz) throws SQLException {
        Object value = res.getObject(columnLabel);
        if (value == null) return Optional.empty();
        Assert.isInstanceOf(clazz, value, String.format("参数类型不匹配 %s not %s", clazz, value.getClass()));
        return Optional.ofNullable((T) value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObject(ResultSet res, String columnLabel, Class<T> clazz) throws SQLException {
        Object value = res.getObject(columnLabel);
        Preconditions.checkNotNull(value, "数据列%s对应的数值为null,与期望值不符.", columnLabel);
        Assert.isInstanceOf(clazz, value, String.format("参数类型不匹配%s not %s", clazz, value.getClass()));
        return (T) value;
    }


    public static Range<Long> getLongRange(ResultSet res, String columnLabel) throws SQLException {
        String range = res.getString(columnLabel);
        Preconditions.checkNotNull(range, "%s 对应的 Range<Long> 值不可以为空...", columnLabel);
        boolean start_open = StringUtils.startsWith(range, "(");
        boolean end_opne = StringUtils.startsWith(range, ")");
        String[] values = StringUtils.split(StringUtils.removeAll(range, "[\\[()\\]]"), "..");
        if (start_open) {
            return end_opne ? Range.open(Long.valueOf(values[0]), Long.valueOf(values[1])) :
                    Range.openClosed(Long.valueOf(values[0]), Long.valueOf(values[1]));
        } else {
            return end_opne ? Range.closedOpen(Long.valueOf(values[0]), Long.valueOf(values[1])) :
                    Range.closed(Long.valueOf(values[0]), Long.valueOf(values[1]));
        }
    }

}
