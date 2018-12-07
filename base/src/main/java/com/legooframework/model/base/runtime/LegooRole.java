package com.legooframework.model.base.runtime;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.base.entity.Sorting;

public class LegooRole implements Sorting {
    private final String roleNo, roleName;
    private final int priority;

    public LegooRole(String roleNo, String roleName, int priority) {
        this.roleNo = roleNo;
        this.roleName = roleName;
        this.priority = priority;
    }

    public String getRoleNo() {
        return roleNo;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public int getIndex() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof LegooRole)) return false;
        LegooRole legooRole = (LegooRole) o;
        return priority == legooRole.priority &&
                Objects.equal(roleNo, legooRole.roleNo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roleNo, priority);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("roleNo", roleNo)
                .add("priority", priority)
                .toString();
    }
}
