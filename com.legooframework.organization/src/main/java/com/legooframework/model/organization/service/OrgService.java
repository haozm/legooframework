package com.legooframework.model.organization.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.organization.entity.EmployeeEntityAction;
import com.legooframework.model.organization.entity.StoreEntityAction;

public abstract class OrgService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("organizationBundle", Bundle.class);
    }

    protected StoreEntityAction getStoreEntityAction() {
        return getBean(StoreEntityAction.class);
    }

    protected EmployeeEntityAction getEmployeeAction() {
        return getBean(EmployeeEntityAction.class);
    }

}
