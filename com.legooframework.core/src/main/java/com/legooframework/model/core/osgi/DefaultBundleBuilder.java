package com.legooframework.model.core.osgi;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class DefaultBundleBuilder {

    private String name, version;
    private List<DependsItem> dependsItems;
    private Set<EventItem> listenEvents;

    public DefaultBundleBuilder(String name, String version) {
        this.name = name;
        this.version = version;
        this.dependsItems = Lists.newArrayList();
        this.listenEvents = Sets.newHashSet();
    }

    LegooBundle building() {
        return new LegooBundle(name, version, dependsItems, listenEvents);
    }

    public void setDependsItems(String name, String version, boolean required) {
        DependsItem dependsItem = new DependsItem(name, version, required);
        Preconditions.checkArgument(!this.dependsItems.contains(dependsItem),
                "已经存在对应%s的依赖声明", dependsItem);
        this.dependsItems.add(dependsItem);
    }

    public void setListenEvent(String eventName, String channel) {
        EventItem eventItem = new EventItem(eventName, channel);
        if (!this.listenEvents.contains(eventItem))
            this.listenEvents.add(eventItem);
    }

}
