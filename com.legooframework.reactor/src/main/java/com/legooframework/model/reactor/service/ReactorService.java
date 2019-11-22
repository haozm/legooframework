package com.legooframework.model.reactor.service;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.reactor.entity.RetailFactEntity;
import com.legooframework.model.reactor.entity.RetailFactEntityAction;
import com.legooframework.model.smsgateway.entity.AutoRunChannel;
import com.legooframework.model.smsgateway.entity.SendMessageBuilder;
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

    public void runRetailNewSmsJob() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<Integer>> companyIds = getBean(TemplateEntityAction.class)
                .loadEnabledCompany(TemplateEntity.CLASSIFIES_RIGHTS_AND_INTERESTS);
        if (!companyIds.isPresent()) return;
        for (Integer companyId : companyIds.get()) {
            OrgEntity company = getBean(OrgEntityAction.class).loadComById(companyId);
            Map<String, Object> countMap = getBean(RetailFactEntityAction.class).count4RetailSmsJob(company);
            if (MapUtils.getInteger(countMap, "total", 0) == 0) continue;
            Optional<List<RetailFactEntity>> retailFacts = getBean(RetailFactEntityAction.class)
                    .query4RetailSmsJob(company, MapUtils.getInteger(countMap, "maxId", 0));
            if (!retailFacts.isPresent()) continue;
            Optional<TemplateEntity> tempate =
                    templateAction.findByCompanyWithClassifies(company, TemplateEntity.CLASSIFIES_RIGHTS_AND_INTERESTS);
            Multimap<Integer, RetailFactEntity> multimap = ArrayListMultimap.create();
            retailFacts.get().forEach(x -> multimap.put(x.getStoreId(), x));
            for (Integer storeId : multimap.keySet()) {
                StoEntity store = getBean(StoEntityAction.class).loadById(storeId);
                List<SendMessageBuilder> builders = Lists.newArrayListWithCapacity(multimap.get(storeId).size());
                multimap.get(storeId).forEach(rec -> {
                    SendMessageBuilder builder = SendMessageBuilder.createWithoutJobNoTemplate(BusinessType.RIGHTS_AND_INTERESTS_CARE, 0,
                            AutoRunChannel.SMS_ONLY);
                    builder.setReplaceMap(rec.toReplaceMap());
                });


//                public boolean batchSaveMessage(OrgEntity company, StoEntity store, List< SendMessageBuilder > msgBuilder,
//                        String msgTemplate, UserAuthorEntity user)
            }
        }
        LoginContextHolder.clear();
    }

}
