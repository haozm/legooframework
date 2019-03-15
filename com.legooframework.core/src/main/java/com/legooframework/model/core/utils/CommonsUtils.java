package com.legooframework.model.core.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.legooframework.model.core.base.entity.Sorting;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class CommonsUtils {

    private static final Ordering<Sorting> ordering = Ordering.from((o1, o2) -> Ints.compare(o1.getIndex(), o2.getIndex()));

    public static Ordering<Sorting> getOrdering() {
        return ordering;
    }

    private final static String KEY_STRING = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

    public static String randomId(int lenth) {
        Preconditions.checkArgument(lenth > 0);
        return RandomStringUtils.random(lenth, KEY_STRING);
    }

    public static String transformShortPinYin(String inputString) {
        if (Strings.isNullOrEmpty(inputString)) return null;
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        char[] input = (inputString).trim().toCharArray();
        if (ArrayUtils.isEmpty(input)) return null;
        StringBuilder output = new StringBuilder();
        try {
            for (char anInput : input) {
                if (Character.toString(anInput).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(anInput, format);
                    if (ArrayUtils.isNotEmpty(temp)) {
                        output.append(temp[0], 0, 1);
                    }
                } else {
                    output.append(Character.toString(anInput));
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            return null;
        }
        return output.toString();
    }
}
