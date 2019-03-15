package com.csosm.commons.entity;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.Comparator;
import java.util.List;

// 统一的排序接口
public class OrderAbledUtil {

    private static Ordering<OrderAbled> ORDERING = Ordering.from((o1, o2) -> Ints.compare(o1.getOrdering(), o2.getOrdering()));

    public static <E extends OrderAbled> List<E> sorted(Iterable<E> elements) {
        return ORDERING.sortedCopy(elements);
    }

    public static <E extends OrderAbled> List<E> reverse(Iterable<E> elements) {
        return ORDERING.reverse().sortedCopy(elements);
    }


}
