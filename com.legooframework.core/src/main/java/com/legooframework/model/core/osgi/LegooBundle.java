package com.legooframework.model.core.osgi;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.legooframework.model.core.event.LegooEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LegooBundle implements Bundle {

    private final String name, version;
    private final List<DependsItem> dependsItems;
    private final Set<EventItem> listenEvents;

    LegooBundle(String name, String version, List<DependsItem> dependsItems, Set<EventItem> listenEvents) {
        this.name = name;
        this.version = version;
        this.listenEvents = CollectionUtils.isEmpty(listenEvents) ? null : ImmutableSet.copyOf(listenEvents);
        this.dependsItems = CollectionUtils.isEmpty(dependsItems) ? null : ImmutableList.copyOf(dependsItems);
    }

    @Override
    public Map<String, Object> toDesc() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("Bundle-Name", this.name);
        params.put("Bundle-Version", this.version);
        if (CollectionUtils.isNotEmpty(dependsItems)) {
            params.put("Bundle-Depends", this.dependsItems);
        }
        params.put("Bundle-Status", "OK");
        return params;
    }

    @Override
    public boolean exitsDepends() {
        return CollectionUtils.isNotEmpty(dependsItems);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Optional<String> getChannelByEvent(LegooEvent event) {
        if (CollectionUtils.isEmpty(listenEvents)) return Optional.empty();

        Optional<EventItem> optional = this.listenEvents.stream()
                .filter(e -> e.isSameEvent(event)).findFirst();
        return optional.map(EventItem::getChannel);
    }

    @Override
    public List<DependsItem> getDepends() {
        return null;
    }

    @Override
    public boolean accept(Message<?> message) {
        if (CollectionUtils.isEmpty(listenEvents)) return false;
        MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
        String eventTarget = (String) accessor.getHeader("eventTarget");
        String eventName = (String) accessor.getHeader("eventName");
        if (StringUtils.equals("*", eventTarget)) {
            Optional<EventItem> opt
                    = this.listenEvents.stream().filter(x -> x.isEvent(eventName)).findFirst();
            return opt.isPresent();
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("version", version)
                .add("dependsItems", dependsItems)
                .add("listenEvents", listenEvents)
                .toString();
    }


}
