package com.legooframework.model.membercare.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.membercare.entity.TaskSwitchEntity;
import com.legooframework.model.membercare.entity.TaskType;
import com.legooframework.model.membercare.entity.Touch90JobEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;

import java.util.List;
import java.util.Optional;

public class MemerCareJobService extends MemberCareService {

    private static final Logger logger = LoggerFactory.getLogger(MemerCareJobService.class);

    public void runTouch90JobByCompany(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "公司ID不可以为空....");
        Optional<TaskSwitchEntity> taskSwitch = getTaskSwitchAction().getTouch90Task(company);
        if (taskSwitch.isPresent()) {
            Preconditions.checkState(taskSwitch.get().isEnabled(), "当前公司尚未启用90服务...");
        } else {
            if (logger.isWarnEnabled())
                logger.warn(String.format("公司%s 尚未启用 touch90Job 服务....", company.getName()));
            return;
        } // end_if
        Optional<JobExecution> jobExecution = getLegooJobService()
                .getLastJobExecution(TaskType.Touche90.getDesc(), company.getId());
        Touch90JobEntity touch90Job = null;
        if (jobExecution.isPresent()) {
            touch90Job = Touch90JobEntity.getInstance(jobExecution.get().getJobInstance().getId(),
                    company, jobExecution.get().getJobParameters());
            Optional<Touch90JobEntity> next = touch90Job.nextJob();
            if (next.isPresent()) {
                getLegooJobService().runJob(TaskType.Touche90.getDesc(), company.getId().longValue(),
                        next.get().currentJobParameters());
            }
        } else {
            touch90Job = Touch90JobEntity.init(company);
            getLegooJobService().runJob(TaskType.Touche90.getDesc(), company.getId().longValue(),
                    touch90Job.currentJobParameters());
        }
    }

    public void runTouchJob() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<CrmOrganizationEntity>> companies = getCompanyAction().loadAllCompany();
        if (!companies.isPresent()) return;
        final List<CrmOrganizationEntity> enabledComs = Lists.newArrayList();
        companies.get().forEach(com -> {
            Optional<TaskSwitchEntity> taskSwitch = getTaskSwitchAction().getTouch90Task(com);
            if (taskSwitch.isPresent() && taskSwitch.get().isEnabled()) {
                enabledComs.add(com);
            } else {
                if (logger.isWarnEnabled())
                    logger.warn(String.format("公司%s 尚未启用抑或禁用 touch90Job 服务....", com.getName()));
            }
        });
        if (CollectionUtils.isEmpty(enabledComs)) return;
        enabledComs.forEach(this::runTouch90JobByCompany);
    }
}
