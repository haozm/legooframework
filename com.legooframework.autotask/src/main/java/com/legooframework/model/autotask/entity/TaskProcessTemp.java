package com.legooframework.model.autotask.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.covariant.entity.EmpEntity;
import com.legooframework.model.covariant.entity.MemberEntity;
import com.legooframework.model.covariant.entity.WxUserEntity;

import java.util.UUID;

public class TaskProcessTemp {

    private final String uuid;
    private final TaskRuleEntity taskRule;
    private final String template;
    private final boolean error;
    // weixnId, mainwxId, deviceId
    // uid openid,null
    // userId,phone,name
    private String sendInfo01, sendInfo02, sendInfo03;

    private TaskProcessTemp(TaskRuleEntity taskRule, boolean error, String template, String sendInfo01,
                            String sendInfo02, String sendInfo03) {
        this.uuid = UUID.randomUUID().toString();
        this.taskRule = taskRule;
        this.template = template;
        this.error = error;
        this.sendInfo01 = sendInfo01;
        this.sendInfo02 = sendInfo02;
        this.sendInfo03 = sendInfo03;
    }

    TaskRuleEntity getTaskRule() {
        return taskRule;
    }

    boolean isError() {
        return error;
    }

    String getTemplate() {
        return template;
    }

    public static TaskProcessTemp creatSms4Member(TaskRuleEntity taskRule, String template, MemberEntity member) {
        return new TaskProcessTemp(taskRule, false, template, String.valueOf(member.getId()), member.getPhone(), member.getName());
    }

    public static TaskProcessTemp creatWx4Wechat(TaskRuleEntity taskRule, String template, WxUserEntity user) {
        return new TaskProcessTemp(taskRule, false, template, user.getUserName(), user.getBindWxUserName(), user.getDevicesId());
    }

    public static TaskProcessTemp creatErrorWx4Wechat(TaskRuleEntity taskRule, String errMsg, WxUserEntity user) {
        if (user != null) {
            return new TaskProcessTemp(taskRule, true, errMsg, user.getUserName(), user.getBindWxUserName(), user.getDevicesId());
        } else {
            return new TaskProcessTemp(taskRule, true, errMsg, null, null, null);
        }
    }

    public static TaskProcessTemp creatSmsError4Member(TaskRuleEntity taskRule, String errMsg, MemberEntity member) {
        if (member != null) {
            return new TaskProcessTemp(taskRule, true, errMsg, String.valueOf(member.getId()), member.getPhone(), member.getName());
        } else {
            return new TaskProcessTemp(taskRule, true, errMsg, null, null, null);
        }
    }

    public static TaskProcessTemp creatSms4Employee(TaskRuleEntity taskRule, String template, EmpEntity employee) {
        return new TaskProcessTemp(taskRule, false, template, String.valueOf(employee.getId()), employee.getPhone(), employee.getName());
    }

    public static TaskProcessTemp creatSmsError4Employee(TaskRuleEntity taskRule, String errMsg, EmpEntity employee) {
        if (employee != null) {
            return new TaskProcessTemp(taskRule, true, errMsg, String.valueOf(employee.getId()), employee.getPhone(), employee.getName());
        } else {
            return new TaskProcessTemp(taskRule, true, errMsg, null, null, null);
        }
    }

    String getUuid() {
        return uuid;
    }

    String getSendInfo01() {
        return sendInfo01;
    }

    String getSendInfo02() {
        return sendInfo02;
    }

    String getSendInfo03() {
        return sendInfo03;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("error", error)
                .add("taskRule", taskRule.getId())
                .add("template", template)
                .toString();
    }
}
