package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CircleReadStatus implements Cloneable {

    private String weixinId;
    private boolean read;

    private CircleReadStatus(String weixinId, boolean read) {
        this.weixinId = weixinId;
        this.read = read;
    }

    boolean isWeixin(String weixinId) {
        return StringUtils.equals(this.weixinId, weixinId);
    }

    void setReaded() {
        this.read = true;
    }

    static String join(Collection<CircleReadStatus> readStatuses) {
        return CollectionUtils.isEmpty(readStatuses) ? null :
                StringUtils.join(readStatuses.stream().map(CircleReadStatus::toString).collect(Collectors.toList()), ',');
    }

    static List<CircleReadStatus> split(String read_status) {
        if (!Strings.isNullOrEmpty(read_status)) {
            List<CircleReadStatus> _temp = Lists.newArrayList();
            String[] items = StringUtils.split(read_status, ',');
            Stream.of(items).forEach(item -> _temp.add(CircleReadStatus.createByDB(item)));
            return _temp;
        }
        return null;
    }

    static CircleReadStatus create(String weixinId) {
        return new CircleReadStatus(weixinId, false);
    }

    static CircleReadStatus createByDB(String data) {
        String[] args = StringUtils.split(data, ':');
        return new CircleReadStatus(args[0], StringUtils.equals("1", args[1]));
    }

    String getWeixinId() {
        return weixinId;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CircleReadStatus that = (CircleReadStatus) o;
        return read == that.read &&
                Objects.equal(weixinId, that.weixinId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(weixinId, read);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", weixinId, read ? 1 : 0);
    }
}
