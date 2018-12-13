package com.legooframework.model.tenant.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class TenantDto {

    private final String id, name;

    public TenantDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantDto tenantDto = (TenantDto) o;
        return Objects.equal(id, tenantDto.id) &&
                Objects.equal(name, tenantDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}
