package com.legooframework.model.autotask.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.autotask.entity.*;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.SendChannel;
import com.legooframework.model.covariant.service.CovariantService;
import org.joda.time.LocalDateTime;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AutoTaskService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(AutoTaskService.class);

    private int count = 0;

    public void buildTaskExecutes() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<TaskSwitchEntity>> switches_on = getBean(TaskSwitchEntityAction.class).findSwitchesOn();
        if (!switches_on.isPresent()) return;
        long count = getBean(TaskSourceEntityAction.class).queryTask4TodoCount(switches_on.get());
        if (count == 0) return;
        Job job = getBean("buildTaskJob", Job.class);
        JobParametersBuilder jb = new JobParametersBuilder();
        Map<String, Object> params_step01 = Maps.newHashMap();
        List<String> switches_on_str = switches_on.get().stream().map(TaskSwitchEntity::toSqlString).collect(Collectors.toList());
        params_step01.put("step01.switch", Joiner.on('#').join(switches_on_str));
        jb.addString("step01.params", Joiner.on('$').withKeyValueSeparator('=').join(params_step01));
        jb.addDate("job.tamptime", LocalDateTime.now().toDate());
        JobParameters jobParameters = jb.toJobParameters();
        try {
            getJobLauncher().run(job, jobParameters);
        } catch (Exception e) {
            logger.error("buildTaskExecutes() has error", e);
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 重启后 重新加载任务到任务队列中
     */
    public void loadTaskExecutesJob() {
        if (count >= 1) return;
        LoginContextHolder.setAnonymousCtx();
        try {
            Optional<List<TaskExecuteEntity>> tasks = getTaskExecuteAction().findTaskExecute4Jobs();
            if (!tasks.isPresent()) return;
            for (TaskExecuteEntity task : tasks.get()) {
                if (DelayType.NO_DELAY == task.getDelayType()) continue;
                try {
                    scheduleJob(task);
                } catch (Exception e) {
                    logger.error("loadJobTaskExecutes() has error...", e);
                    task.setError(e.getMessage());
                }
            }
        } finally {
            count++;
            LoginContextHolder.clear();
        }
    }

    private void scheduleJob(TaskExecuteEntity task) throws Exception {
        TaskExecuteJobDetailBuilder jobDetailBuilder = getTaskExecuteJobDetail();
        jobDetailBuilder.setTaskExecute(task);
        JobDetail jobDetail = jobDetailBuilder.getJobBuilder().build();
        Trigger trigger = task.createTrigger();
        if (!getScheduler().checkExists(jobDetail.getKey())) {
            getScheduler().scheduleJob(jobDetail, trigger);
        }
    }

    /**
     * 睡觉万岁
     *
     * @param task 我是谁啊  --- 我是三叶虫
     */
    public void doneTaskExecute(@Payload TaskExecuteEntity task) {
        LoginContextHolder.setAnonymousCtx();
        TaskExecuteEntity res = null;
        try {
            if (logger.isDebugEnabled())
                logger.debug(String.format("---- sendingMessage(%s) start...", task));
            if (SendChannel.SMS == task.getSendChannel()) {
                res = sendBySms(task);
            } else if (SendChannel.WECHAT == task.getSendChannel()) {
                res = sendByWx(task);
            } else if (SendChannel.WECHAT_GZH == task.getSendChannel()) {
                res = task.setError("尚不支持的 公众号 发送渠道....");
            } else {
                res = task.setError("尚不支持的发送渠道....");
            }
        } finally {
            getTaskExecuteAction().batchUpdateStatus(Lists.newArrayList(res));
            if (logger.isDebugEnabled())
                logger.debug(String.format("---- sendingMessage(%s) end...", res));
            LoginContextHolder.clear();
        }
    }

    /**
     * 万恶的微信发送
     *
     * @param task 我是量子包
     */
    private TaskExecuteEntity sendByWx(TaskExecuteEntity task) {
        Optional<Integer> storeId = task.getStoreId();
        try {
            if (storeId.isPresent()) {
                getBean(CovariantService.class).sendWxMsgByStore(task.getTemplate(), null,
                        Lists.newArrayList(task.getSendInfo01()), storeId.get(), task.getBusinessType());
                return task.setFinished();
            } else {
                return task.setError("无门店信息....");
            }
        } catch (Exception e) {
            logger.error(String.format("sendByWx(%s) has error...", task), e);
            return task.setError(e.getMessage());
        }
    }

    /**
     * 发送短信服务
     *
     * @param task TaskExecuteEntity
     */
    private TaskExecuteEntity sendBySms(TaskExecuteEntity task) {
        Integer companyId = task.getStoreId().orElse(0);
        Optional<Integer> storeId = task.getStoreId();
        try {
            if (storeId.isPresent()) {
                getBean(CovariantService.class).sendSmsByStore(storeId.get(), task.getTemplate(), task.getSendInfo02(),
                        task.getSendInfo03(), task.getBusinessType(), task.getUuid());
            } else {
                getBean(CovariantService.class).sendSmsByCompany(companyId, task.getTemplate(), task.getSendInfo02(),
                        task.getSendInfo03(), task.getBusinessType(), task.getUuid());
            }
            return task.setFinished();
        } catch (Exception e) {
            logger.error(String.format("sendSms(%s) has error...", task), e);
            return task.setError(e.getMessage());
        }
    }

}
