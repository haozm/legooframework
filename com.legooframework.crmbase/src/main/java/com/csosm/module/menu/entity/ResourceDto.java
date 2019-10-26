package com.csosm.module.menu.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceDto {

    private String id, name, url, desc, icon;
    private List<ResourceDto> children;

    public ResourceDto(String id, String name, String url, String icon, String desc) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.desc = desc;
        this.children = Lists.newArrayList();
    }

    public void filter(Collection<String> pathIds) {
        if (!CollectionUtils.isEmpty(children)) {
            List<ResourceDto> removes =
                    children.stream().filter(x -> !pathIds.contains(x.getId())).collect(Collectors.toList());
            children.removeAll(removes);
            if (!CollectionUtils.isEmpty(children)) {
                for (ResourceDto $it : children) {
                    $it.filter(pathIds);
                }
            }
        }
    }

    public void addChild(ResourceDto dto) {
        this.children.add(dto);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }

    public String getDesc() {
        return desc;
    }

    public List<ResourceDto> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("icon", icon)
                .add("url", url)
                .add("desc", desc)
                .add("children", children)
                .toString();
    }
}
