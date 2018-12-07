package com.legooframework.model.event;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.base.runtime.LoginContext;
import org.apache.commons.collections4.MapUtils;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

// 基础事件定义
public abstract class LegooEvent {
    // 事件请求源Bundle Name 以及 目标Bundle Name
    private final String source, target, eventName;
    private final boolean command;
    protected final Map<String, Object> payload;

    protected LegooEvent(String source, String target, String eventName) {
        this.source = source;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(source), "事件源不可以为空.");
        this.target = target;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(eventName), "事件名称不可以为空.");
        this.eventName = eventName;
        this.command = true;
        this.payload = Maps.newHashMap();
    }

    protected LegooEvent(String source, String eventName) {
        this.source = source;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(source), "事件源不可以为空.");
        this.target = "*";
        Preconditions.checkArgument(!Strings.isNullOrEmpty(eventName), "事件名称不可以为空.");
        this.eventName = eventName;
        this.command = false;
        this.payload = Maps.newHashMap();
    }

    protected void putPayload(String key, Object value) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "事件负载数据索引不可以为空.");
        this.payload.put(key, value);
    }

    protected void putAll(Map<String, Object> data) {
        this.payload.putAll(data);
    }

    protected final String getString(String key) {
        return MapUtils.getString(this.payload, key);
    }

    public boolean isCommand() {
        return command;
    }

    public String getSource() {
        return source;
    }

    public String getEventName() {
        return eventName;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getValue(String key, Class<T> clazz) {
        Object value = MapUtils.getObject(this.payload, key);
        if (null == value) return Optional.empty();
        Assert.isInstanceOf(clazz, value,
                String.format("Key=%s 对应的值 %s 与期望类型 %s 不匹配.", key, value.getClass(), clazz.getName()));
        return Optional.of((T) value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getNullAbleValue(String key, Class<T> clazz) {
        Object value = MapUtils.getObject(this.payload, key);
        if (null == value) return null;
        Assert.isInstanceOf(clazz, value,
                String.format("Key=%s 对应的值 %s 与期望类型 %s 不匹配.", key, value.getClass(), clazz.getName()));
        return (T) value;
    }

    protected Map<String, Object> getPayload() {
        return payload;
    }

    public String getTarget() {
        return target;
    }

    public Message<LegooEvent> toMessage(LoginContext loginContext) {
        return MessageBuilder.withPayload(this)
                .setHeader("eventType", isCommand() ? "command" : "publish")
                .setHeader("eventName", getEventName())
                .setHeader("loginContext", loginContext)
                .setHeader("eventTarget", getTarget())
                .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .add("target", target)
                .add("eventName", eventName)
                .add("command", command)
                .add("payload", payload)
                .toString();
    }
}
