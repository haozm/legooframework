package com.legooframework.model.upload.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathHelper {

    private PathHelper() {
    }

    /**
     * 删除空格符、回车符、tab符
     *
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * 提取xml的文件中的变量
     *
     * @param path
     * @return
     */
    public static List<String> getPathVars(String path) {
        List<String> ls = new ArrayList<String>();
        Pattern pattern = Pattern.compile("(?<=\\{)(.+?)(?=\\})");
        Matcher matcher = pattern.matcher(path);
        while (matcher.find())
            ls.add(matcher.group());
        return ls;
    }

}
