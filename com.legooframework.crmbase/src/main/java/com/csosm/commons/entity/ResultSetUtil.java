package com.csosm.commons.entity;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public abstract class ResultSetUtil {

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getOptValue(ResultSet resultSet, String label, Class<T> clazz)
            throws SQLException {
        Object value = resultSet.getObject(label);
        if (value == null) return Optional.absent();
        Assert.isInstanceOf(clazz, value);
        return Optional.of((T) value);
    }

    public static Optional<String> getOptStrValue(ResultSet resultSet, String label) throws SQLException {
        Object value = resultSet.getObject(label);
        if (value == null) return Optional.absent();
        Assert.isInstanceOf(String.class, value);
        return Optional.of((String) value);
    }

    public static String getStrValue(ResultSet resultSet, String label) throws SQLException {
        Optional<String> value = getOptStrValue(resultSet, label);
        Preconditions.checkState(value.isPresent(), " %s 对应的数值不可以为空...", label);
        return value.get();
    }


    public static Optional<Set<Integer>> splitterToInts(ResultSet resultSet, String label)
            throws SQLException {
        Optional<String> storeIds = getOptValue(resultSet, label, String.class);
        if (!storeIds.isPresent()) return Optional.absent();
        Set<Integer> integerSet = Sets.newHashSet();
        List<String> _id_list = splitter.splitToList(storeIds.get());
        for (String $it : _id_list) integerSet.add(Integer.valueOf($it));
        return Optional.fromNullable(CollectionUtils.isEmpty(integerSet) ? null : integerSet);
    }

    private static Splitter splitter = Splitter.on(',');
}
