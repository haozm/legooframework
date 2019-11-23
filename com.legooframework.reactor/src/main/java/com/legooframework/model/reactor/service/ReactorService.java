package com.legooframework.model.reactor.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.TemplateEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.reactor.entity.RetailFactAgg;
import com.legooframework.model.reactor.entity.RetailFactEntityAction;
import com.legooframework.model.smsgateway.entity.AutoRunChannel;
import com.legooframework.model.smsgateway.entity.SendMessageAgg;
import com.legooframework.model.smsgateway.entity.SendMessageBuilder;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReactorService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(ReactorService.class);

    public void runRetailSmsJob() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<Integer>> companyIds = templateAction
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

    /**
     * listener
     *
     * @param payload OOXX
     */
    public void reactorMessageHandler(@Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("Handle Event Message %s", payload.toString()));
        try {
            if (payload instanceof RetailFactAgg) {
                RetailFactAgg agg = (RetailFactAgg) payload;
                if (agg.hasError()) {
                    logger.warn(String.format("[%s] has error,drop it...", payload.toString()));
                    return;
                }
                if (super.containsBean("smsgateway-subscribe-channel")) {
                    SendMessageAgg sendMessageAgg = new SendMessageAgg(agg.getCompanyId(), agg.getStore().getId());
                    sendMessageAgg.addBuilder(SendMessageBuilder
                            .createWithoutJobNoTemplate(BusinessType.RIGHTS_AND_INTERESTS_CARE, 0, AutoRunChannel.SMS_ONLY));
                    Message<SendMessageAgg> msg_request = MessageBuilder.withPayload(sendMessageAgg)
                            .setHeader("modelName", "com.legooframework.reactor").build();
                    messagingTemplate.send("smsgateway-subscribe-channel", msg_request);
                    if (logger.isDebugEnabled())
                        logger.debug(String.format("Send Message [%s] to smsgateway-subscribe-channel is ok ...", sendMessageAgg));
                }
            }
        } catch (Exception e) {
            logger.error("reactorMessageHandler(...)", e);
        }
    }

}
