package com.legooframework.model.takecare.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.covariant.entity.EmpEntityAction;
import com.legooframework.model.covariant.service.CovariantService;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("takeCareBundle", Bundle.class);
    }

    EmpEntityAction getEmployeeAction() {
        return getBean(EmpEntityAction.class);
    }

    CovariantService getCovariantService() {
        return getBean(CovariantService.class);
    }

}
