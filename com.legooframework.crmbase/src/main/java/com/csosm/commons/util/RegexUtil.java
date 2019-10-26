package com.csosm.commons.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexUtil {

    public static String formatTxt(String txtTemp, Map<String, String> params) {
        Pattern pattern = Pattern.compile("\\{[^}]*}");
        Matcher matcher = pattern.matcher(txtTemp);
        List<String> keys = Lists.newArrayListWithCapacity(8);
        List<String> values = Lists.newArrayListWithCapacity(8);
        String k, v;
        while (matcher.find()) {
            k = matcher.group();
            v = MapUtils.getString(params, k);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(v), " %s 对应的替换值不存在", k);
            keys.add(k);
            values.add(v);
        }
        if (CollectionUtils.isEmpty(keys)) return txtTemp;
        return StringUtils.replaceEach(txtTemp, keys.toArray(new String[]{}), values.toArray(new String[]{}));
    }

    public static boolean likePattern(String txt) {
        if (Strings.isNullOrEmpty(txt)) return false;
        return Pattern.matches("^%\\w*%$", txt);
    }

}
