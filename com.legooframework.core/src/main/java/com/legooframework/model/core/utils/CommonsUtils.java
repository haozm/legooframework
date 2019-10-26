package com.legooframework.model.core.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.legooframework.model.core.base.entity.Sorting;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

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

    public static Long random10Num() {
        return RandomUtils.nextLong(1000000000L, 9999999999L);
    }

}
