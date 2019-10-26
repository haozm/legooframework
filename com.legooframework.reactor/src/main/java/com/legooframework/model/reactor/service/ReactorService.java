package com.legooframework.model.reactor.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.TemplateEntity;
import com.legooframework.model.covariant.entity.TemplateEntityAction;
import com.legooframework.model.reactor.entity.RetailFactEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReactorService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(ReactorService.class);

    public void runRetailSmsJob() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<Integer>> companyIds = getBean(TemplateEntityAction.class)
                .loadEnabledCompany(TemplateEntity.CLASSIFIES_RIGHTS_AND_INTERESTS);
        if (!companyIds.isPresent()) return;
        Job job = getBean("retailSmsJob", Job.class);
        for (Integer companyId : companyIds.get()) {
            OrgEntity company = getBean(OrgEntityAction.class).loadComById(companyId);
            Map<String, Object> countMap = getBean(RetailFactEntityAction.class).count4RetailSmsJob(company);
            if (MapUtils.getInteger(countMap, "total", 0) == 0) continue;
            JobParametersBuilder jb = new JobParametersBuilder();
            Map<String, Object> params = Maps.newHashMap();
            params.put("sql", "query4RetailSmsJob");
            params.put("stmtId", "RetailFactEntity.query4list");
            params.put("companyId", companyId);
            params.put("maxId", MapUtils.getInteger(countMap, "maxId", 0));
            params.put("companyShortName", company.getShortName());
            jb.addString("job.params", Joiner.on('$').withKeyValueSeparator('=').join(params));
            jb.addDate("job.tamptime", LocalDateTime.now().toDate());
            JobParameters jobParameters = jb.toJobParameters();
            try {
                getJobLauncher().run(job, jobParameters);
            } catch (Exception e) {
                logger.error("runRetailSmsJob() has error", e);
            }
        }
        LoginContextHolder.clear();
    }

}
