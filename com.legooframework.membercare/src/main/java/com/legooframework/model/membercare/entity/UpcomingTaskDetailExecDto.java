package com.legooframework.model.membercare.entity;

import com.google.common.base.Strings;
import com.legooframework.model.core.utils.WebUtils;

public class UpcomingTaskDetailExecDto {

    private final UpcomingTaskDetailEntity upcomingTaskDetail;
    private final String templateClassifies;
    private String template;
    private AutoRunChannel autoRunChannel;

    public UpcomingTaskDetailExecDto(UpcomingTaskDetailEntity detail, String template, AutoRunChannel autoRunChannel) {
        this.upcomingTaskDetail = detail;
        this.templateClassifies = String.format("%s_%s_%s_%s", detail.getBusinessType().toString(),
                detail.getTenantId(), detail.getCategories(), detail.getSubRuleId());
        this.template = template;
        this.autoRunChannel = autoRunChannel;
    }

    public String getCompanyAndStoreIds() {
        return String.format("%s_%s", getCompanyId(), getStoreId());
    }

    UpcomingTaskDetailExecDto(UpcomingTaskDetailEntity detail, AutoRunChannel autoRunChannel) {
        this.upcomingTaskDetail = detail;
        this.templateClassifies = String.format("%s_%s_%s_%s", detail.getBusinessType().toString(),
                detail.getTenantId(), detail.getCategories(), detail.getSubRuleId());
        this.autoRunChannel = autoRunChannel;
    }

    public boolean hasRunChannel() {
        return this.autoRunChannel != null;
    }

    public void setTemplate(String template) {
        if (Strings.isNullOrEmpty(this.template))
            this.template = template;
    }

    public Integer getStoreId() {
        return upcomingTaskDetail.getStoreId();
    }

    public Integer getCompanyId() {
        return upcomingTaskDetail.getCompanyId();
    }

    public String getTemplateClassifies() {
        return templateClassifies;
    }

    public UpcomingTaskDetailEntity getTaskDetail() {
        return upcomingTaskDetail;
    }

    public boolean isExitsTemplate() {
        return !Strings.isNullOrEmpty(this.template);
    }

    public String toStringWithEncoding() {
        return String.format("%s,%s,%s||%s", upcomingTaskDetail.getId(), upcomingTaskDetail.getMemberId(),
                autoRunChannel.getChannel(), WebUtils.encodeUrl(template));
    }
}
