package com.legooframework.model.autotask.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.RoleType;
import com.legooframework.model.covariant.entity.SendChannel;
import org.joda.time.LocalDateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TaskExecuteEntity extends BaseEntity<Long> implements BatchSetter {

    private final Integer companyId, storeId;
    private final long sourceId;
    private final BusinessType businessType;
    private final SendChannel sendChannel;
    private final DelayType delayType;
    private final RoleType sendTarget;
    private final int ruleId;
    private final String template, uuid, delayTime;
    private String message;
    private int status; // 0:init  1:执行中...2:入Job队列 8:finished  9: error
    // weixnId, mainwxId, deviceId
    // uid openid,null
    // userId,phone,name
    private String sendInfo01, sendInfo02, sendInfo03;
    private LocalDateTime generationTime, planExecuteTime;

    public CronTrigger createTrigger() {
        String cronExpression = String.format("0 %d %d %d %d ? %d", planExecuteTime.getMinuteOfHour(), planExecuteTime.getHourOfDay(),
                planExecuteTime.getDayOfMonth(), planExecuteTime.getMonthOfYear(), planExecuteTime.getYear());
        return TriggerBuilder.newTrigger()
                .withIdentity(String.format("Trigger:%s", this.uuid))
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

    public SendChannel getSendChannel() {
        return sendChannel;
    }

    public DelayType getDelayType() {
        return delayType;
    }

    String getMessage() {
        return message;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    int getStatus() {
        return status;
    }

    public String getTemplate() {
        return template;
    }

    public String getSendInfo01() {
        return sendInfo01;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSendInfo02() {
        return sendInfo02;
    }

    public String getSendInfo03() {
        return sendInfo03;
    }

    TaskExecuteEntity(Long id, ResultSet res) {
        super(id);
        try {
            this.businessType = BusinessType.paras(res.getInt("business_type"));
            this.delayType = DelayType.paras(res.getInt("delay_type"));
            this.sendChannel = SendChannel.paras(res.getInt("send_channel"));
            this.companyId = ResultSetUtil.getObject(res, "company_id", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "store_id", Integer.class);
            this.status = ResultSetUtil.getObject(res, "status", Integer.class);
            this.sendTarget = RoleType.parasStr(ResultSetUtil.getString(res, "send_target"));
            this.sourceId = ResultSetUtil.getObject(res, "source_id", Long.class);
            this.ruleId = ResultSetUtil.getObject(res, "rule_id", Integer.class);
            this.template = ResultSetUtil.getOptString(res, "template", null);
            this.message = ResultSetUtil.getOptString(res, "message", null);
            this.delayTime = ResultSetUtil.getString(res, "delay_time");
            this.uuid = ResultSetUtil.getString(res, "uuid");
            this.sendInfo01 = ResultSetUtil.getOptString(res, "send_info01", null);
            this.sendInfo02 = ResultSetUtil.getOptString(res, "send_info02", null);
            this.sendInfo03 = ResultSetUtil.getOptString(res, "send_info03", null);
            this.planExecuteTime = ResultSetUtil.getLocalDateTime(res, "plan_execute_time");
            this.generationTime = ResultSetUtil.getLocalDateTime(res, "generation_time");
        } catch (SQLException e) {
            throw new RuntimeException("Restore TaskExecuteEntity has SQLException", e);
        }
    }

    public TaskExecuteEntity setError(String message) {
        TaskExecuteEntity clone = (TaskExecuteEntity) cloneMe();
        clone.status = 9;
        clone.message = message;
        return clone;
    }

    public Optional<Integer> getStoreId() {
        return Optional.ofNullable(storeId == 0 ? null : storeId);
    }

    public TaskExecuteEntity setFinished() {
        TaskExecuteEntity clone = (TaskExecuteEntity) cloneMe();
        clone.status = 8;
        clone.message = "任务执行完毕";
        return clone;
    }

    public TaskExecuteEntity setJobQueue() {
        TaskExecuteEntity clone = (TaskExecuteEntity) cloneMe();
        clone.status = 2;
        clone.message = "进入Job队列";
        return clone;
    }

    public TaskExecuteEntity setRuning() {
        TaskExecuteEntity clone = (TaskExecuteEntity) cloneMe();
        clone.status = 1;
        clone.message = "任务执行中...";
        return clone;
    }

    TaskExecuteEntity(TaskSourceEntity taskSource, TaskProcessTemp taskTemp) {
        super(0L);
        this.sourceId = taskSource.getId();
        this.companyId = taskSource.getCompanyId();
        this.businessType = taskSource.getBusinessType();
        this.generationTime = taskSource.getGenerationTime();
        this.storeId = taskSource.getStoreId().orElse(0);
        this.ruleId = taskTemp.getTaskRule().getId();
        this.sendChannel = taskTemp.getTaskRule().getSendChannel();
        this.sendTarget = taskTemp.getTaskRule().getSendTarget();
        this.delayTime = taskTemp.getTaskRule().getDelayTime();
        this.delayType = taskTemp.getTaskRule().getDelayType();
        this.planExecuteTime = taskTemp.getTaskRule().getDelayTime(taskSource.getGenerationTime());
        if (taskTemp.isError()) {
            this.status = 9;
            this.template = null;
            this.message = taskTemp.getTemplate();
        } else {
            this.status = 0;
            this.template = taskTemp.getTemplate();
            this.message = null;
        }
        this.uuid = taskTemp.getUuid();
        // weixnId, mainwxId, deviceId
        // uid openid,null
        // memberId,phone,name
        this.sendInfo01 = taskTemp.getSendInfo01();
        this.sendInfo02 = taskTemp.getSendInfo02();
        this.sendInfo03 = taskTemp.getSendInfo03();
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // company_id, org_id, store_id, status, source_id, rule_id, send_channel, business_type, delay_type,
        ps.setObject(1, this.companyId);
        ps.setObject(2, 0);
        ps.setObject(3, this.storeId);
        ps.setObject(4, this.status);
        ps.setObject(5, this.sourceId);
        ps.setObject(6, this.ruleId);
        ps.setObject(7, this.sendChannel.getValue());
        ps.setObject(8, this.businessType.getValue());
        ps.setObject(9, this.delayType.getValue());
        // plan_execute_time, send_info01, send_info02, send_info03, message, template, uuid, delete_flag, tenant_id，
        ps.setObject(10, this.planExecuteTime.toDate());
        ps.setObject(11, this.sendInfo01);
        ps.setObject(12, this.sendInfo02);
        ps.setObject(13, this.sendInfo03);
        ps.setObject(14, this.message);
        ps.setObject(15, this.template);
        ps.setObject(16, this.uuid);
        ps.setObject(17, this.companyId);
        // generation_time
        ps.setObject(18, this.generationTime.toDate());
        ps.setObject(19, this.delayTime);
        ps.setObject(20, this.sendTarget.getName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("sourceId", sourceId)
                .add("businessType", businessType)
                .add("ruleId", ruleId)
                .add("delayTime", delayTime)
                .add("sendChannel", sendChannel)
                .add("delayType", delayType)
                .add("sendTarget", sendTarget)
                .add("planExecuteTime", planExecuteTime.toString("yyyy-MM-dd HH:mm:ss"))
                .add("generationTime", generationTime.toString("yyyy-MM-dd HH:mm:ss"))
                .add("template", template)
                .add("message", message)
                .add("uuid", uuid)
                .add("status", status)
                .add("sendInfo01", sendInfo01)
                .add("sendInfo02", sendInfo02)
                .add("sendInfo03", sendInfo03)
                .toString();
    }
}
