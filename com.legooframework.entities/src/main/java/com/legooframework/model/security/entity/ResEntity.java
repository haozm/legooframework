package com.legooframework.model.security.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.security.dto.ResourceDto;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

public abstract class ResEntity extends BaseEntity<String> {

    private final ResourceType type;
    private final String name;
    private final String desc;
    private final List<ResEntity> subRes;
    private List<String> paths;

    ResEntity(String id, ResourceType type, String name, String desc, Long tenantId) {
        super(id, tenantId, -1L);
        this.name = name;
        this.desc = desc;
        Preconditions.checkNotNull(type, "资源类型不可以为空.");
        this.type = type;
        this.paths = Lists.newArrayList();
        this.subRes = Lists.newArrayList();
    }

    public void addSubRes(ResEntity resource) {
        Preconditions.checkNotNull(resource);
        if (isPage()) {
            Preconditions.checkArgument(resource.isButton(),
                    "Page 资源下只能添加 Button 或者 SubPage 资源.");
        } else if (isButton()) {
            Preconditions.checkArgument(resource.isMenu(), "Button 资源无下级资源。");
        } else if (isMenu()) {
            Preconditions.checkArgument(!resource.isButton(), "Menu 资源 不支持 按钮关联。");
        }
        if (this.subRes.contains(resource)) return;
        this.subRes.add(resource);
        resource.setPaths(this);
    }

    void setPaths(ResEntity root) {
        if (root == null) {
            this.paths.add(this.getId());
        } else {
            this.paths = Lists.newArrayList(root.getPaths());
            this.paths.add(this.getId());
        }
    }

    public List<String> getPaths() {
        return paths;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isMenu() {
        return ResourceType.Menu == this.type;
    }

    public boolean isPage() {
        return ResourceType.Page == this.type;
    }

    public boolean isButton() {
        return ResourceType.Button == this.type;
    }

    public boolean isRestful() {
        return ResourceType.Restful == this.type;
    }

    public Optional<List<ResEntity>> getSubRes() {
        return Optional.ofNullable(CollectionUtils.isEmpty(subRes) ? null : ImmutableList.copyOf(subRes));
    }

    public boolean hasSubRes() {
        return !CollectionUtils.isEmpty(subRes);
    }

    public ResourceDto createDto() {
        ResourceDto dto = new ResourceDto(getId(), name, null, null, desc);
        if (hasSubRes()) {
            for (ResEntity $it : subRes)
                dto.addChild($it.createDto());
        }
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResEntity resEntity = (ResEntity) o;
        return type == resEntity.type &&
                Objects.equal(name, resEntity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), type, name, desc);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("tenantId", getTenantId())
                .add("type", type)
                .add("name", name)
                .add("desc", desc)
                .add("subRes's size ", CollectionUtils.isEmpty(subRes) ? 0 : subRes.size())
                .add("paths", paths)
                .toString();
    }
}
