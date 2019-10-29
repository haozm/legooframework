package com.legooframework.model.salesrecords.service;

import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.salesrecords.entity.SaleRecordEntityAction;
import org.springframework.batch.core.launch.JobLauncher;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("salesRecordsBundle", Bundle.class);
    }

    OrgEntityAction getCompanyAct() {
        return getBean(OrgEntityAction.class);
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

    JobLauncher getJobLauncher() {
        return getBean("jobLauncher", JobLauncher.class);
    }
}
