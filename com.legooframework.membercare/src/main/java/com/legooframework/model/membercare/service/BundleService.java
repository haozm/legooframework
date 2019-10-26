package com.legooframework.model.membercare.service;

import com.legooframework.model.batchsupport.entity.JobInstanceEntityAction;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.membercare.entity.TaskCareRuleEntityAction;
import com.legooframework.model.membercare.entity.TaskSwitchEntityAction;
import com.legooframework.model.membercare.entity.Touch90CareLogEntityAction;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("memberCareBundle", Bundle.class);
    }

    JdbcQuerySupport getJdbcQuery() {
        return getBean("careJdbcQuerySupport", JdbcQuerySupport.class);
    }

    JobInstanceEntityAction getJobInstanceAction() {
        return getBean(JobInstanceEntityAction.class);
    }

    LuncherCareJobService getLegooJobService() {
        return getBean("luncherCareJobService", LuncherCareJobService.class);
    }
}
