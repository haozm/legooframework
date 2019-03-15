package com.csosm.commons.util;

import com.csosm.commons.entity.Replaceable;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceWordsUtil {

    private ReplaceWordsUtil() {
        throw new AssertionError();
    }

    public static boolean hasReplaceWord(String msg) {
        Pattern p = Pattern.compile("(\\{[^\\}]*\\})");
        Matcher m = p.matcher(msg);
        return m.find();
    }


    public static String replaceToBlank(String msg) {
        return msg.replaceAll("(\\{[^\\}]*\\})", "");
    }

    public static String replace(String msg, Replaceable... replaces) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(msg), "需要替换的内容msg不能为空");
        if (ArrayUtils.isEmpty(replaces)) return msg;
        if (!hasReplaceWord(msg)) return msg;
        Map<String, String> words = Maps.newHashMap();
        for (Replaceable replace : replaces)
            words.putAll(replace.toSmsMap(null));
        return formatTxt(msg, words);
    }

    public static String formatTxt(String txtTemp, Map<String, String> params) {
        Pattern pattern = Pattern.compile("\\{[^}]*}");
        Matcher matcher = pattern.matcher(txtTemp);
        List<String> keys = Lists.newArrayListWithCapacity(8);
        List<String> values = Lists.newArrayListWithCapacity(8);
        String k, v;
        while (matcher.find()) {
            k = matcher.group();
            v = MapUtils.getString(params, k);
            if (Strings.isNullOrEmpty(v)) v = "";
            keys.add(k);
            values.add(v);
        }
        if (CollectionUtils.isEmpty(keys)) return txtTemp;
        return StringUtils.replaceEach(txtTemp, keys.toArray(new String[]{}), values.toArray(new String[]{}));
    }

    public static String replace(String msg, Map<String, String> replace) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(msg), "需要替换的内容msg不能为空");
        if (replace.isEmpty()) return msg;
        if (!hasReplaceWord(msg)) return msg;
        return formatTxt(msg, replace);
    }

}