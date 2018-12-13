package com.legooframework.model.organization.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.organization.entity.CompanyEntity;
import com.legooframework.model.organization.entity.EmployeeEntity;
import com.legooframework.model.organization.entity.StoreEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Optional;

public class EmployeeAgg {

    private final EmployeeEntity employee;
    private Collection<StoreEntity> stores;
    private final CompanyEntity company;

    public EmployeeAgg(EmployeeEntity employee, CompanyEntity company, Collection<StoreEntity> stores) {
        Preconditions.checkNotNull(employee);
        Preconditions.checkNotNull(company);
        this.employee = employee;
        this.stores = stores;
        this.company = company;
        Preconditions.checkState(employee.isSameTenant(company), "异常数据，职员所属公司数据异常....");
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public Optional<Collection<StoreEntity>> getStores() {
        return Optional.ofNullable(CollectionUtils.isEmpty(stores) ? null : stores);
    }

    public StoreEntity loadStore() {
        Preconditions.checkState(CollectionUtils.isNotEmpty(stores), "职员所在门店信息为空...");
        Preconditions.checkState(stores.size() == 1, "当前职员绑定多家门店，该方法不支持调用...");
        return stores.iterator().next();
    }

    public CompanyEntity getCompany() {
        return company;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("employee", employee)
                .add("stores's size ", CollectionUtils.isEmpty(stores) ? 0 : this.stores.size())
                .add("company", company)
                .toString();
    }
}
