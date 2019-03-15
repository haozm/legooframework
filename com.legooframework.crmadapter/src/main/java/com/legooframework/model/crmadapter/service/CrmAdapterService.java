package com.legooframework.model.crmadapter.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.CrmEmployeeEntityAction;
import com.legooframework.model.crmadapter.entity.CrmMemberEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;

public abstract class CrmAdapterService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("crmAdapterBundle", Bundle.class);
    }

    CrmOrganizationEntityAction getOrgAction() {
        return getBean(CrmOrganizationEntityAction.class);
    }

    CrmStoreEntityAction getStoreAction() {
        return getBean(CrmStoreEntityAction.class);
    }

    CrmEmployeeEntityAction getEmployeeAction() {
        return getBean(CrmEmployeeEntityAction.class);
    }

    CrmMemberEntityAction getMemberAction() {
        return getBean(CrmMemberEntityAction.class);
    }

}
