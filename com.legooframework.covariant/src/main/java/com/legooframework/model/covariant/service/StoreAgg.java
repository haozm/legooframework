package com.legooframework.model.covariant.service;

import com.google.common.base.MoreObjects;
import com.legooframework.model.covariant.entity.EmpEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.ToReplace;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StoreAgg implements ToReplace {

    private final StoEntity store;
    private final List<EmpEntity> employees;

    StoreAgg(StoEntity store, List<EmpEntity> employees) {
        this.store = store;
        this.employees = employees;
    }

    Optional<List<EmpEntity>> findStoreManagers() {
        if (CollectionUtils.isEmpty(employees)) return Optional.empty();
        List<EmpEntity> emps = employees.stream().filter(EmpEntity::isStoreManager).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(emps) ? null : emps);
    }

    Optional<List<EmpEntity>> findShoppingGuides() {
        if (CollectionUtils.isEmpty(employees)) return Optional.empty();
        List<EmpEntity> emps = employees.stream().filter(EmpEntity::isShoppingGuide).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(emps) ? null : emps);
    }

    StoEntity getStore() {
        return store;
    }

    boolean hasEmps() {
        return CollectionUtils.isNotEmpty(employees);
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        return store.toReplaceMap();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("store's id", store.getId())
                .add("employees' size", CollectionUtils.isEmpty(employees) ? null : employees.size())
                .toString();
    }
}
