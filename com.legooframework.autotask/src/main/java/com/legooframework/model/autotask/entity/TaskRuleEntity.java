package com.legooframework.model.autotask.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.StringSubstitutor;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class TaskRuleEntity extends BaseEntity<Integer> implements BatchSetter {

    private Integer companyId, orgId, storeId;
    private BusinessType businessType;
    private DelayType delayType;// 0: 及时发送  1: 顺延发送  2：顺延定时发送
    // day=0,hour=0,minute=0
    private String delayTime;
    private SendChannel sendChannel; // 0/1/2 短信 微信 公众号
    private RoleType sendTarget; // 本人/导购/店长/经理/公司领导
    private String template;
    private boolean enabled; // 是否启用

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("sourceId", getId());
        params.put("businessType", businessType.getValue());
        params.put("sendChannel", sendChannel.getValue());
        params.put("sendTarget", sendTarget.getName());
        params.put("delayType", delayType.getValue());
        params.put("orgId", orgId);
        params.put("enabled", enabled ? 1 : 0);
        return params;
    }

    TaskRuleEntity(OrgEntity comopany, BusinessType businessType, DelayType delayType, String delayTime,
                   SendChannel sendChannel, RoleType sendTarget, String template) {
        super(0);
        this.companyId = comopany.getId();
        this.orgId = 0;
        this.storeId = 0;
        this.businessType = businessType;
        this.delayType = delayType;
        this.delayTime = delayTime;
        this.sendChannel = sendChannel;
        this.sendTarget = sendTarget;
        this.template = template;
        this.enabled = true;
    }

    TaskRuleEntity(StoEntity store, BusinessType businessType, DelayType delayType, String delayTime,
                   SendChannel sendChannel, RoleType sendTarget, String template) {
        super(0);
        this.companyId = store.getCompanyId();
        this.orgId = store.getOrgId();
        this.storeId = store.getId();
        this.businessType = businessType;
        this.delayType = delayType;
        this.delayTime = delayTime;
        this.sendChannel = sendChannel;
        this.sendTarget = sendTarget;
        this.template = template;
        this.enabled = true;
    }

    TaskRuleEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.businessType = BusinessType.paras(res.getInt("business_type"));
            this.delayType = DelayType.paras(res.getInt("delay_type"));
            this.sendChannel = SendChannel.paras(res.getInt("send_channel"));
            this.companyId = ResultSetUtil.getObject(res, "company_id", Integer.class);
            this.orgId = res.getInt("org_id") == 0 ? null : res.getInt("org_id");
            this.storeId = res.getInt("store_id") == 0 ? null : res.getInt("store_id");
            this.template = ResultSetUtil.getOptString(res, "template", null);
            this.sendTarget = RoleType.parasStr(ResultSetUtil.getString(res, "send_target"));
            this.delayTime = ResultSetUtil.getOptString(res, "delay_time", null);
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
        } catch (SQLException e) {
            throw new RuntimeException("Restore TaskRuleEntity has SQLException", e);
        }
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // company_id, org_id, store_id, business_type, delay_type, delay_time, send_channel,
        ps.setObject(1, this.companyId);
        ps.setObject(2, this.orgId);
        ps.setObject(3, this.storeId);
        ps.setObject(4, this.businessType.getValue());
        ps.setObject(5, this.delayType.getValue());
        ps.setObject(6, this.delayTime);
        ps.setObject(7, this.sendChannel.getValue());
//        send_target, template, enabled, delete_flag, tenant_id
        ps.setObject(8, this.sendTarget.getName());
        ps.setObject(9, this.template);
        ps.setObject(10, enabled ? 1 : 0);
        ps.setObject(11, this.companyId);
    }

    LocalDateTime getDelayTime(LocalDateTime generationTime) {
        LocalDateTime planDateTime = generationTime;
        Map<String, String> params = Splitter.on(',').withKeyValueSeparator('=').split(this.delayTime);
        int day = MapUtils.getIntValue(params, "day", 0);
        int hour = MapUtils.getIntValue(params, "hour", 0);
        int minute = MapUtils.getIntValue(params, "minute", 0);
        if (DelayType.POSTPONE_DELAY == this.delayType) {
            planDateTime = planDateTime.plusMinutes(minute + hour * 60 + day * 24 * 60);
        } else if (DelayType.TIMING_DELAY == this.delayType) {
            if (day > 0) planDateTime = planDateTime.plusDays(day);
            if (hour != 0 || minute != 0) {
                planDateTime = new LocalDateTime(planDateTime.getYear(), planDateTime.getMonthOfYear(),
                        planDateTime.getDayOfMonth(), hour, minute, 0);
            }
        } else {
            planDateTime = LocalDateTime.now();
        }
        return planDateTime;
    }

    boolean isSameRule(TaskRuleEntity that) {
        return Objects.equals(this.companyId, that.companyId) && Objects.equals(this.storeId, that.storeId)
                && this.businessType == that.businessType;
    }

    public SendChannel getSendChannel() {
        return sendChannel;
    }

    DelayType getDelayType() {
        return delayType;
    }

    String getDelayTime() {
        return delayTime;
    }

    public RoleType getSendTarget() {
        return sendTarget;
    }

    private void setDelayTime(int day, int hour, int minute) {
        if (day != 0)
            Preconditions.checkArgument(day >= 0, "天取值非法取值 day=%s", day);
        if (hour != 0)
            Preconditions.checkArgument(hour >= 0 && hour <= 24, "小时取值范围是[0,24],非法取值 hour=%s", hour);
        if (minute != 0)
            Preconditions.checkArgument(minute >= 0 && minute <= 60, "分钟取值范围是[0,60],非法取值 minute=%s", minute);
        if (DelayType.POSTPONE_DELAY == this.delayType || DelayType.TIMING_DELAY == this.delayType)
            Preconditions.checkArgument(day != 0 || hour != 0 || minute != 0, "延时发送取值不可以为空值...");
        this.delayTime = String.format("day=%d,hour=%d,minute=%d", day, hour, minute);
    }

    private void setTemplate(String template) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "模板内容不可以为空");
        this.template = template;
    }

    boolean isEnabled() {
        return enabled;
    }

    boolean isSameBusinessType(TaskSourceEntity taskSource) {
        return Objects.equals(companyId, taskSource.getCompanyId()) && this.businessType == taskSource.getBusinessType();
    }

    boolean isSameBusinessType(BusinessType type) {
        return this.businessType == type;
    }

    public String getTemplate() {
        return template;
    }

    boolean isCompany() {
        return this.orgId == null && this.storeId == null;
    }

    boolean isOnlyCompany(OrgEntity company) {
        return Objects.equals(this.companyId, company.getId()) && this.storeId == null;
    }

    boolean isStore() {
        return this.storeId != null;
    }

    boolean isStore(StoEntity store) {
        return Objects.equals(this.storeId, store.getId()) && Objects.equals(this.companyId, store.getCompanyId());
    }

    public String replace(Map<String, Object> params) throws TemplateReplaceException {
        if (MapUtils.isEmpty(params)) return this.template;
        try {
            StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
            return substitutor.replace(this.template);
        } catch (Exception e) {
            throw new TemplateReplaceException(String.format("模板 %s 替换发送异常...%s", this.template, params), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskRuleEntity that = (TaskRuleEntity) o;
        return businessType == that.businessType &&
                delayType == that.delayType &&
                sendChannel == that.sendChannel &&
                enabled == that.enabled &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(orgId, that.orgId) &&
                Objects.equals(delayTime, that.delayTime) &&
                Objects.equals(storeId, that.storeId) &&
                Objects.equals(template, that.template) &&
                Objects.equals(sendTarget, that.sendTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, orgId, storeId, businessType, delayType, delayTime, sendChannel, template,
                sendTarget, enabled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("orgId", orgId)
                .add("storeId", storeId)
                .add("businessType", businessType)
                .add("delayType", delayType)
                .add("sendChannel", sendChannel)
                .add("template", template)
                .add("sendTarget", sendTarget)
                .add("delayTime", delayTime)
                .add("enabled", enabled)
                .toString();
    }
}
