package com.legooframework.model.membercare.service;

import com.legooframework.model.batchsupport.service.LegooJobService;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.membercare.entity.CareRuleEntityAction;
import com.legooframework.model.membercare.entity.TaskSwitchEntityAction;
import com.legooframework.model.membercare.entity.Touch90CareLogEntityAction;

public abstract class MemberCareService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("memberCareBundle", Bundle.class);
    }

    Touch90CareLogEntityAction getTouch90LogAction() {
        return getBean(Touch90CareLogEntityAction.class);
    }

    TaskSwitchEntityAction getTaskSwitchAction() {
        return getBean(TaskSwitchEntityAction.class);
    }

    CrmOrganizationEntityAction getCompanyAction() {
        return getBean(CrmOrganizationEntityAction.class);
    }

    CareRuleEntityAction getCareRuleEntityAction(){
        return getBean(CareRuleEntityAction.class);
    }

    LegooJobService getLegooJobService() {
        return getBean("legooJobService", LegooJobService.class);
    }
}
