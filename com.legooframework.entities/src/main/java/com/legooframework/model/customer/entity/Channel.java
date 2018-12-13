package com.legooframework.model.customer.entity;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum Channel {
    TYPE_MEMBER(1, "会员"),
    TYPE_WEIXIN(2, "微信");
//    TYPE_PUBLICR(3, "公众号");

    private final int val;
    private final String name;

    Channel(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public static Channel valueOf(int val) {
        Optional<Channel> opt = Arrays.stream(values()).filter(x -> x.val == val).findFirst();
        Preconditions.checkArgument(opt.isPresent(), "不存在type=%s 对应的会员渠道类型", val);
        return opt.get();
    }

    public int getVal() {
        return val;
    }

    public String getName() {
        return name;
    }

    public static Set<Integer> getVals() {
        return Arrays.stream(values()).map(x -> x.getVal()).collect(Collectors.toSet());
    }
}
