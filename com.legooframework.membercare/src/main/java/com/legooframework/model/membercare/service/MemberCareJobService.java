package com.legooframework.model.membercare.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.batchsupport.entity.JobInstanceEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.membercare.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.*;
import java.util.stream.Collectors;

public class MemberCareJobService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(MemberCareJobService.class);

    public void automaticStartingTask() {
        LoginContextHolder.setAnonymousCtx();
        try {
            getBean(UpcomingTaskDetailEntityAction.class).startDetails();
        } finally {
            LoginContextHolder.clear();
        }
    }

    public void automaticExtensionedTask() {
        LoginContextHolder.setAnonymousCtx();
        try {
            getBean(UpcomingTaskDetailEntityAction.class).extensionedDetails();
        } finally {
            LoginContextHolder.clear();
        }
    }

    public void automaticExpiredTask() {
        LoginContextHolder.setAnonymousCtx();
        try {
            getBean(UpcomingTaskDetailEntityAction.class).expiredDetails();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 订阅你要订阅的事件
     */
    @SuppressWarnings("unchecked")
    public void subscribeBusEvent(@Header(value = "user") LoginContext user, @Header(value = "action") String action,
                                  @Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("subscribeBusEvent(user,action:%s,payload) start", action));
        if (StringUtils.equals(TaskCareRuleConstant.ACTION_TOUCH90RULEDIFFERENCE, action)) {
            @SuppressWarnings("unchecked")
            List<Touch90RuleDifference> differences = (List<Touch90RuleDifference>) payload;
            List<Touch90RuleDifference> ruleDisabledList = differences.stream().filter(Touch90RuleDifference::isRuleDisabled)
                    .collect(Collectors.toList());
            List<Touch90RuleDifference> subNodeRemoveList = differences.stream().filter(Touch90RuleDifference::isNodeRemove)
                    .collect(Collectors.toList());
            List<Touch90RuleDifference> ruleRemoveList = differences.stream().filter(Touch90RuleDifference::isRuleRemove)
                    .collect(Collectors.toList());
            List<Touch90RuleDifference> closeAutoList = differences.stream().filter(Touch90RuleDifference::isCloseAuto)
                    .collect(Collectors.toList());
            List<Touch90RuleDifference> changeTimeList = differences.stream().filter(Touch90RuleDifference::isChangeTime)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(ruleDisabledList)) {
                // getBean(UpcomingTaskDetailEntityAction.class).execByEvent(ruleDisabledList, 4);
                // 巴拉巴拉 小魔仙 之 任务引擎 也需要一切关闭
                Set<String[]> jobParams = ruleDisabledList.stream().map(Touch90RuleDifference::toJobParams)
                        .collect(Collectors.toSet());
                getBean(JobInstanceEntityAction.class).disableJobByParams(BusinessType.TOUCHED90.getJobName(), jobParams);
            } else if (CollectionUtils.isNotEmpty(subNodeRemoveList)) {
                getBean(UpcomingTaskDetailEntityAction.class).execByEvent(subNodeRemoveList, 0);
            } else if (CollectionUtils.isNotEmpty(ruleRemoveList)) {
                getBean(UpcomingTaskDetailEntityAction.class).execByEvent(ruleRemoveList, 8);
                // 巴拉巴拉 小魔仙 之 任务引擎 也需要一切关闭
                Set<String[]> jobParams = ruleRemoveList.stream().map(Touch90RuleDifference::toJobParams)
                        .collect(Collectors.toSet());
                getBean(JobInstanceEntityAction.class).disableJobByParams(BusinessType.TOUCHED90.getJobName(), jobParams);
            } else if (CollectionUtils.isNotEmpty(closeAutoList)) {
                getBean(UpcomingTaskDetailEntityAction.class).execByEvent(closeAutoList, 6);
            } else if (CollectionUtils.isNotEmpty(changeTimeList)) {
                getBean(UpcomingTaskDetailEntityAction.class).execByEvent(changeTimeList, 3);
            }
        } else if (StringUtils.equals(TaskCareRuleConstant.ACTION_TOUCH90RULE_ADD, action)) {
            Touch90RuleDifference ruleAdd = (Touch90RuleDifference) payload;
            getBean(Touch90TemplateEntityAction.class).saveOrUpdate(ruleAdd.getCareRule(), ruleAdd.isIncloudCompany(),
                    ruleAdd.getStores().orElse(null), user);
            if (ruleAdd.getCareRule().isAutoRun()) {
                // 捕获 事件变化情况
                List<String> rule_ids = Lists.newArrayList();
                if (ruleAdd.isIncloudCompany()) {
                    rule_ids.add(String.format("%s_-1_%s_%s",
                            ruleAdd.getCareRule().getCompanyId(), ruleAdd.getCareRule().getBusinessType().toString(),
                            ruleAdd.getCareRule().getCategories()));
                }
                ruleAdd.getStores().ifPresent(stores -> stores.forEach(store -> rule_ids.add(String.format("%s_%s_%s_%s",
                        ruleAdd.getCareRule().getCompanyId(), ruleAdd.getCareRule().getBusinessType().toString(),
                        store.getId(), ruleAdd.getCareRule().getCategories()))));
            }
        }
    }

    /**
     * 按照任务大节点 取消
     *
     * @param taskId 总任务ID
     */
    public void canceledByTask(Integer taskId) {
        Optional<UpcomingTaskEntity> task = getBean(UpcomingTaskEntityAction.class).findById(taskId);
        Preconditions.checkState(task.isPresent(), "Id=%s 对应的任务不存在...");
        UpcomingTaskDetails details = getBean(UpcomingTaskDetailEntityAction.class).loadByTask(task.get());
        Optional<List<UpcomingTaskDetailEntity>> list = details.cancalAll();
        list.ifPresent(x -> getBean(UpcomingTaskDetailEntityAction.class).canceledDetails(x, "按任务ID取消"));
    }

}
