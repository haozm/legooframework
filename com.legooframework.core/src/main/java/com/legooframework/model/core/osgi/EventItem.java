package com.legooframework.model.core.osgi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.event.LegooEvent;
import org.apache.commons.lang3.StringUtils;

class EventItem {

    private final String event, channel;

    public EventItem(String event, String channel) {
        this.event = event;
        this.channel = channel;
    }

    boolean isSameEvent(LegooEvent event) {
        return StringUtils.equals(this.event, event.getEventName());
    }

    boolean isEvent(String eventName) {
        return StringUtils.equals(this.event, eventName);
    }

    String getEvent() {
        return event;
    }

    String getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventItem)) return false;
        EventItem eventItem = (EventItem) o;
        return Objects.equal(event, eventItem.event) &&
                Objects.equal(channel, eventItem.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(event, channel);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("event", event)
                .add("channel", channel)
                .toString();
    }
}
