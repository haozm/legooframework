package com.legooframework.model.covariant.service;

import com.google.common.base.MoreObjects;
import com.legooframework.model.covariant.entity.EmpEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.ToReplace;

import java.util.Map;

public class EmployeeAgg implements ToReplace {

    private final EmpEntity employee;
    private final StoEntity store;

    EmployeeAgg(EmpEntity employee, StoEntity store) {
        this.employee = employee;
        this.store = store;
    }

    public EmpEntity getEmployee() {
        return employee;
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = employee.toReplaceMap();
        if (store != null) params.putAll(store.toReplaceMap());
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("employee", employee.getId())
                .add("store", store == null ? null : store.getId())
                .toString();
    }
}
