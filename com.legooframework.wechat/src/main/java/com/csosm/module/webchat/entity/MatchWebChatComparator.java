package com.csosm.module.webchat.entity;


import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import javax.annotation.Nullable;

public abstract class MatchWebChatComparator {

    public static Ordering<Distance> orderDistance() {
        return Ordering.natural().reverse().onResultOf(new Function<Distance, Double>() {
            @Override
            public Double apply(@Nullable Distance distance) {
                return distance.getJaroWinklerDistance();
            }
        });
    }

    // 越小越好
    private static Ordering<Distance> orderJaccard() {
        return Ordering.natural().onResultOf(new Function<Distance, Double>() {
            @Override
            public Double apply(@Nullable Distance distance) {
                return distance.getJaccardDistance();
            }
        });
    }

    // 越大越好
    private static Ordering<Distance> orderLevenshtein() {
        return Ordering.natural().onResultOf(new Function<Distance, Double>() {
            @Override
            public Double apply(@Nullable Distance distance) {
                return distance.getLevenshteinDistance();
            }
        });
    }

    // 大一些好
    private static Ordering<Distance> orderJaroWinkler() {
        return Ordering.natural().reverse().onResultOf(new Function<Distance, Double>() {
            @Override
            public Double apply(@Nullable Distance distance) {
                return distance.getJaroWinklerDistance();
            }
        });
    }


}
