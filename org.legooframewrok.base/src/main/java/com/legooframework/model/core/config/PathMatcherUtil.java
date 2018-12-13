package com.legooframework.model.core.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PathMatcherUtil {

    public static boolean match(String[] patterns, String path) {
        for (String $it : patterns) {
            Pattern pattern = Pattern.compile($it);
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) return true;
        }
        return false;
    }
}
