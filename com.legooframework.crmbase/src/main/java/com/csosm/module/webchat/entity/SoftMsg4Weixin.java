package com.csosm.module.webchat.entity;

import com.csosm.commons.util.MyWebUtil;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoftMsg4Weixin {

    private final String title;
    private String desc;
    private final String url;
    

    public SoftMsg4Weixin(String msg) {
        if (!SoftMsg4Weixin.isSoftInfo(msg))
            throw new IllegalArgumentException(String.format("文本[%s] 不符合软文格式(@###@ 分隔)", msg));
        List<String> list = MyWebUtil.splitSoftInfo(msg);
        if (!list.get(2).startsWith("http"))
            throw new IllegalArgumentException(String.format("url[%s] 不符合http格式", list.get(2)));
        this.title = list.get(0);
        this.desc = list.get(1);
        this.url = list.get(2);

    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getUrl() {
        return url;
    }

    static boolean isSoftInfo(String msg) {
        if (Strings.isNullOrEmpty(msg)) return false;
        int result = appearNumber(msg, "@###@");
        return result == 2;
    }

    private static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoftMsg4Weixin)) return false;
        SoftMsg4Weixin softInfo = (SoftMsg4Weixin) o;
        return Objects.equal(title, softInfo.title) &&
                Objects.equal(desc, softInfo.desc) &&
                Objects.equal(url, softInfo.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title, desc, url);
    }

    @Override
    public String toString() {
        return String.format("SoftMsg4Weixin [title=%s, desc=%s, url=%s]", title, desc, url);
    }

}