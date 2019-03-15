package com.legooframework.model.membercare.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.*;
import com.legooframework.model.crmadapter.service.CrmReadService;
import com.legooframework.model.membercare.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MemerCareJobService extends MemberCareService {

    private static final Logger logger = LoggerFactory.getLogger(MemerCareJobService.class);

    public void automaticStartingTask() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<UpcomingTaskDetailEntity>> init_list = getBean(UpcomingTaskEntityAction.class)
                .loadTouch90DetailByStauts(TaskStatus.Create);
        if (!init_list.isPresent()) return;
        List<UpcomingTaskDetailEntity> start_list = Lists.newArrayList();
        for (UpcomingTaskDetailEntity item : init_list.get()) {
            Optional<UpcomingTaskDetailEntity> clone = item.makeStarting();
            clone.ifPresent(start_list::add);
        }
        if (CollectionUtils.isEmpty(start_list)) return;
        getBean(UpcomingTaskEntityAction.class).updateDetailToStart(start_list);
    }

    public void automaticExpiredTask() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<UpcomingTaskDetailEntity>> start_list = getBean(UpcomingTaskEntityAction.class)
                .loadTouch90DetailByStauts(TaskStatus.Starting);
        if (!start_list.isPresent()) return;
        List<UpcomingTaskDetailEntity> expire_list = Lists.newArrayList();
        for (UpcomingTaskDetailEntity item : start_list.get()) {
            Optional<UpcomingTaskDetailEntity> clone = item.makeExpired();
            clone.ifPresent(expire_list::add);
        }
        if (CollectionUtils.isEmpty(expire_list)) return;
        getBean(UpcomingTaskEntityAction.class).updateDetailToExpired(expire_list);
    }

    public void runTouch90JobByCompany(CrmOrganizationEntity company) {
        Optional<JobExecution> jobExecution = getLegooJobService()
                .getLastJobExecution(TaskType.Touche90.getDesc(), company.getId());
        Touch90JobEntity touch90Job = null;
        if (jobExecution.isPresent()) {
            touch90Job = Touch90JobEntity.getInstance(jobExecution.get().getJobInstance().getId(),
                    company, jobExecution.get().getJobParameters());
            Optional<Touch90JobEntity> next = touch90Job.nextJobParameters();
            next.ifPresent(job90 -> getLegooJobService().runJob(TaskType.Touche90.getDesc(), company.getId().longValue(),
                    job90.currentJobParameters()));
        } else {
            touch90Job = Touch90JobEntity.init(company);
            getLegooJobService().runJob(TaskType.Touche90.getDesc(), company.getId().longValue(),
                    touch90Job.currentJobParameters());
        }
    }

    public void runTouchJob() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<TaskSwitchEntity>> taskSwitchs = getTaskSwitchAction().queryAllTouch90Switch();
        if (!taskSwitchs.isPresent()) return;
        final List<CrmOrganizationEntity> enabled_list = Lists.newArrayList();
        for (TaskSwitchEntity switched : taskSwitchs.get()) {
            if (!switched.isEnabled()) continue;
            Optional<CrmOrganizationEntity> company = getCompanyAction().findCompanyById(switched.getCompanyId());
            if (!company.isPresent()) {
                logger.warn(String.format("公司 %s 不存在，执行90任务中止...", switched.getCompanyId()));
                continue;
            }
            enabled_list.add(company.get());
        }
        if (CollectionUtils.isEmpty(enabled_list)) return;
        enabled_list.forEach(this::runTouch90JobByCompany);
    }

    /**
     * 保存或者更新 touch90 服务
     *
     * @param companyId
     * @param employeeId
     * @param rewrite
     * @param enabled
     * @param automatic
     * @param maxConsumptionDays
     * @param maxAmountOfconsumption
     * @param ruleDetails
     */
    public void saveOrUpdate90Rule(Integer companyId, Integer employeeId,
                                   boolean enabled, boolean automatic,
                                   Integer maxConsumptionDays, Integer maxAmountOfconsumption,
                                   boolean concalBefore, String ruleDetails,
                                   boolean rewrite, Collection<Integer> storeIds) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("saveOrUpdate90Rule(%s, %s , %s, %s, %s, %s, %s, %s)", companyId, employeeId,
                    enabled, automatic, maxConsumptionDays, maxAmountOfconsumption, ruleDetails, rewrite));
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class)
                .findCompanyById(companyId);

        Preconditions.checkState(company.isPresent(), "companyId = %s 对应的公司不存在...", companyId);
        Optional<CrmEmployeeEntity> employee = getBean(CrmEmployeeEntityAction.class)
                .findById(company.get(), employeeId);
        Preconditions.checkState(employee.isPresent(), "职员ID=%s 的员工不存在...", employeeId);
        if (CollectionUtils.isNotEmpty(storeIds)) {
            Optional<List<CrmStoreEntity>> stores = getBean(CrmStoreEntityAction.class).findByIds(company.get(), storeIds);
            stores.ifPresent(x -> {
                getCareRuleEntityAction().saveOrUpdate90Rule(company.get(), x, enabled,
                        automatic, maxConsumptionDays, maxAmountOfconsumption, concalBefore, ruleDetails, true);
            });
            return;
        }

        if (Objects.equals(employee.get().getOrganizationId(), company.get().getId())) {
            getCareRuleEntityAction().saveOrUpdate90Rule(company.get(), null, enabled, automatic, maxConsumptionDays,
                    maxAmountOfconsumption, concalBefore, ruleDetails, rewrite);
        } else if (employee.get().isOnlyStore()) {
            Preconditions.checkState(employee.get().getStoreId().isPresent(), "当前职员无门店信息...");
            Optional<CrmStoreEntity> stores = getBean(CrmStoreEntityAction.class).findById(company.get(),
                    employee.get().getStoreId().get());
            Preconditions.checkState(stores.isPresent(), "职员对应的门店%s 不存在...", employee.get().getStoreId().get());
            getCareRuleEntityAction().saveOrUpdate90Rule(company.get(), Lists.newArrayList(stores.get()), enabled,
                    automatic, maxConsumptionDays, maxAmountOfconsumption, concalBefore, ruleDetails, true);
        } else {
            Integer orgId = employee.get().getOrganizationId();
            Optional<Collection<CrmStoreEntity>> stores = getBean(CrmReadService.class).loadSubStores(companyId, orgId);
            Preconditions.checkState(stores.isPresent(), "ID=%s的组织下无门店可供设置90规则...", orgId);
            getCareRuleEntityAction().saveOrUpdate90Rule(company.get(), stores.get(), enabled, automatic, maxConsumptionDays,
                    maxAmountOfconsumption, concalBefore, ruleDetails, rewrite);
        }
    }

}
