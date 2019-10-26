package com.legooframework.model.core.osgi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class DependsItem {

    private final String name, version;
    private final boolean required;

    DependsItem(String name, String version, boolean required) {
        this.name = name;
        this.version = version;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DependsItem)) return false;
        DependsItem that = (DependsItem) o;
        return Objects.equal(getName(), that.getName()) &&
                Objects.equal(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getVersion());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("version", version)
                .add("required", required)
                .toString();
    }
}
