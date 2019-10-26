package com.legooframework.model.scheduler.mvc;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.scheduler.entity.JobDetailBuilderEnity;
import com.legooframework.model.scheduler.service.ScheduleJobService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping(value = "/job")
public class SchedulerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    @PostMapping(value = "/{jobName}/{groupName}/stop.json")
    public JsonMessage stopJob(@PathVariable(value = "jobName") String jobName,
                               @PathVariable(value = "groupName") String groupName, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("stopJob(request=%s)", request.getRequestURI()));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            getBean(ScheduleJobService.class, request).diabledJob(jobName, groupName);
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/{tigger}/add.json")
    public JsonMessage addNewJob(@PathVariable(value = "tigger") String tigger,
                                 @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addNewJob(requestBody=%s)", requestBody));
        Preconditions.checkArgument(ArrayUtils.contains(new String[]{"cron", "simple"}, tigger), "非法的tigger = %s", tigger);
        Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
        Integer storeId = MapUtils.getInteger(requestBody, "storeId", -1);
        String jobDesc = MapUtils.getString(requestBody, "jobDesc");
        String bundle = MapUtils.getString(requestBody, "bundle");
        String targetBeanName = MapUtils.getString(requestBody, "targetBeanName");
        Preconditions.checkArgument(exitBeanByName(targetBeanName, request), "不存在 targetBeanName = %s 对应的Bean 定义...",
                targetBeanName);
        String targetMethod = MapUtils.getString(requestBody, "targetMethod");
        JobDetailBuilderEnity jobBulder = null;
        if (StringUtils.equals("cron", tigger)) {
            String cronExpression = MapUtils.getString(requestBody, "cronExpression");
            if (companyId == -1 && storeId == -1) {
                jobBulder = JobDetailBuilderEnity.createGeneralCron(jobDesc, bundle, targetBeanName, targetMethod, cronExpression);
            } else if (companyId != -1 && storeId == -1) {
                jobBulder = JobDetailBuilderEnity.createCompanyCron(jobDesc, bundle,
                        targetBeanName, targetMethod, cronExpression, companyId);
            } else {
                jobBulder = JobDetailBuilderEnity.createStoreCron(jobDesc, bundle,
                        targetBeanName, targetMethod, cronExpression, companyId, storeId);
            }
        } else {
            long startDelay = MapUtils.getLong(requestBody, "startDelay", 0L);
            long repeatInterval = MapUtils.getLong(requestBody, "repeatInterval", 0L);
            Preconditions.checkArgument(repeatInterval > 0L, "非法的入参 repeatInterval =%s", repeatInterval);
            if (companyId == -1 && storeId == -1) {
                jobBulder = JobDetailBuilderEnity.createGeneralSimple(jobDesc, bundle,
                        targetBeanName, targetMethod, startDelay, repeatInterval);
            } else if (companyId != -1 && storeId == -1) {
                jobBulder = JobDetailBuilderEnity.createCompanySimple(jobDesc, bundle,
                        targetBeanName, targetMethod, startDelay, repeatInterval, companyId);
            } else {
                jobBulder = JobDetailBuilderEnity.createStoreSimple(jobDesc, bundle,
                        targetBeanName, targetMethod, startDelay, repeatInterval, companyId, storeId);
            }
        }
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            getBean(ScheduleJobService.class, request).addJob(jobBulder);
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
    }


//    @PostMapping(value = "/{jobName}/{groupName}/pause.json")
//    public JsonMessage pauseJob(@PathVariable(value = "jobName") String jobName,
//                                @PathVariable(value = "groupName") String groupName,
//                                HttpServletRequest request) {
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("pauseJob(request=%s)", request.getRequestURI()));
//        LoginContextHolder.setIfNotExitsAnonymousCtx();
//        try {
//            getBean(ScheduleJobService.class, request).pauseJob(jobName, groupName);
//        } finally {
//            LoginContextHolder.clear();
//        }
//        return JsonMessageBuilder.OK().toMessage();
//    }
//
//    @PostMapping(value = "/{jobName}/{groupName}/resume.json")
//    public JsonMessage resumeJob(@PathVariable(value = "jobName") String jobName,
//                                 @PathVariable(value = "groupName") String groupName,
//                                 HttpServletRequest request) {
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("resumeJob(request=%s)", request.getRequestURI()));
//        LoginContextHolder.setIfNotExitsAnonymousCtx();
//        try {
//            getBean(ScheduleJobService.class, request).resumeJob(jobName, groupName);
//        } finally {
//            LoginContextHolder.clear();
//        }
//        return JsonMessageBuilder.OK().toMessage();
//    }

}

