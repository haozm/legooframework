package com.legooframework.model.salesrecords.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.salesrecords.entity.SaleRecordEntityAction;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("salesRecordsBundle", Bundle.class);
    }

    OrgEntityAction getCompanyAct() {
        return getBean(OrgEntityAction.class);
    }

    EmpEntityAction getEmpoyeeAct() {
        return getBean(EmpEntityAction.class);
    }

    MemberEntityAction getMemberAct() {
        return getBean(MemberEntityAction.class);
    }

    StoEntityAction getStoreAct() {
        return getBean(StoEntityAction.class);
    }

    SaleRecordEntityAction getSaleRecordAction() {
        return getBean(SaleRecordEntityAction.class);
    }
}
