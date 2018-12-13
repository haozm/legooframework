package com.legooframework.model.core.base.runtime;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class LegooOrgImpl implements LegooOrg {

    private final Long id;
    private final String name;
    private final int type;
    private final Collection<String> deviceIds;

    private LegooOrgImpl(Long id, String name, int type, Collection<String> deviceIds) {
        this.id = id;
        this.name = name;
        this.type = type;
        if (CollectionUtils.isNotEmpty(deviceIds)) {
            this.deviceIds = ImmutableSet.copyOf(deviceIds);
        } else {
            this.deviceIds = null;
        }
    }

    public static LegooOrg company(Long id, String name) {
        return new LegooOrgImpl(id, name, 0, null);
    }

    public static LegooOrg store(Long id, String name, Collection<String> deviceIds) {
        return new LegooOrgImpl(id, name, 1, deviceIds);
    }

    @Override
    public Optional<Collection<String>> getDeviceIds() {
        return Optional.ofNullable(this.deviceIds);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isCompany() {
        return 0 == this.type;
    }

    @Override
    public boolean isStore() {
        return 1 == this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LegooOrgImpl legooOrg = (LegooOrgImpl) o;
        return type == legooOrg.type &&
                Objects.equals(id, legooOrg.id) &&
                Objects.equals(name, legooOrg.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("type", type)
                .toString();
    }
}
