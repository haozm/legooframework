package com.legooframework.model.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getOptObject(ResultSet res, String columnLabel, Class<T> clazz) throws SQLException {
        Object value = res.getObject(columnLabel);
        if (value == null) return Optional.empty();
        Assert.isInstanceOf(clazz, value, String.format("参数类型不匹配 %s", clazz));
        return Optional.ofNullable((T) value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObject(ResultSet res, String columnLabel, Class<T> clazz) throws SQLException {
        Object value = res.getObject(columnLabel);
        Preconditions.checkNotNull(value, "数据列%s对应的数值为null,与期望值不符.", columnLabel);
        Assert.isInstanceOf(clazz, value, "参数类型不匹配");
        return (T) value;
    }
}
