package com.csosm.commons.entity;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public abstract class IdGenerator {

    private final static String KEY_STRING = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

    public static String randomId(int lenth) {
        Preconditions.checkArgument(lenth > 0);
        return RandomStringUtils.random(lenth, KEY_STRING);
    }

    public static String nextUUIDBefore18Id() {
        return RandomStringUtils.random(18, KEY_STRING);
    }

    public static String nextUUIDBefore8Id() {
        return RandomStringUtils.random(8, KEY_STRING);
    }

    public static String nextUUIDEnd17Id() {
        return RandomStringUtils.random(17, KEY_STRING);
    }

    public static String nextUUIDEnd12Id() {
        return RandomStringUtils.random(12, KEY_STRING);
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

}
