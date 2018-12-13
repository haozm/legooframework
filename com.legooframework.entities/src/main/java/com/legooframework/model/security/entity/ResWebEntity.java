package com.legooframework.model.security.entity;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.security.dto.ResourceDto;

import java.util.Optional;

public class ResWebEntity extends ResEntity {

    private final String url;
    private final String icon;

    public ResWebEntity(String id, ResourceType type, String name, String desc, String url, String icon, Long tenantId) {
        super(id, type, name, desc, tenantId);
        this.url = url;
        this.icon = Strings.emptyToNull(icon);
        Preconditions.checkArgument(ResourceType.Menu != type, "Web资源类型不允许为Menu");
    }

    public String getUrl() {
        return url;
    }

    public String getIcon() {
        return icon;
    }

    public Optional<String> getIconIfExits() {
        return Optional.ofNullable(icon);
    }

    @Override
    public ResourceDto createDto() {
        ResourceDto dto = new ResourceDto(getId(), getName(), url, icon, getDesc());
        if (getSubRes().isPresent()) {
            for (ResEntity $it : getSubRes().get())
                dto.addChild($it.createDto());
        }
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResWebEntity that = (ResWebEntity) o;
        return Objects.equal(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), url);
    }
}
