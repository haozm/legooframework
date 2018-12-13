package com.legooframework.model.salesrecords.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.CrmEmployeeEntityAction;
import com.legooframework.model.crmadapter.entity.CrmMemberEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.salesrecords.entity.SaleRecordEntityAction;

public abstract class AbstractSaleRecordService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("salesRecordsBundle", Bundle.class);
    }

    CrmOrganizationEntityAction getCompanyAct() {
        return getBean(CrmOrganizationEntityAction.class);
    }

    CrmEmployeeEntityAction getEmpoyeeAct() {
        return getBean(CrmEmployeeEntityAction.class);
    }

    CrmMemberEntityAction getMemberAct() {
        return getBean(CrmMemberEntityAction.class);
    }

    CrmStoreEntityAction getStoreAct() {
        return getBean(CrmStoreEntityAction.class);
    }

    SaleRecordEntityAction getSaleRecordAction() {
        return getBean(SaleRecordEntityAction.class);
    }
}
