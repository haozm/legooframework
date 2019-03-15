package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class MsgTemplateEntity extends BaseEntity<String> {

    private Integer companyId, orgId, storeId;
    private String template;
    private Set<String> classifies;
    private Set<TemplateUseScope> useScopes;
    private LocalDate expireDate;
    private boolean enabled;

    // 附加字段信息
    MsgTemplateEntity(CrmOrganizationEntity organization, String template,
                      Collection<String> classifies, Collection<TemplateUseScope> useScopes,
                      LocalDate expireDate) {
        super(UUID.randomUUID().toString());
        this.companyId = organization.isCompany() ? organization.getId() : organization.getCompanyId();
        this.orgId = organization.isCompany() ? -1 : organization.getId();
        this.storeId = -1;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "模板内容不可以为空值...");
        this.template = template;
        this.classifies = Sets.newHashSet(classifies);
        this.useScopes = Sets.newHashSet(useScopes);
        this.expireDate = expireDate;
        this.enabled = true;
    }

    MsgTemplateEntity(CrmStoreEntity store, String template,
                      Collection<String> classifies, Collection<TemplateUseScope> useScopes,
                      LocalDate expireDate) {
        super(UUID.randomUUID().toString());
        this.companyId = store.getCompanyId();
        this.orgId = -1;
        this.storeId = store.getId();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "模板内容不可以为空值...");
        this.template = template;
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(classifies), "模板分类不可为空...");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(useScopes), "模板使用场景不可为空...");
        this.classifies = Sets.newHashSet(classifies);
        this.useScopes = Sets.newHashSet(useScopes);
        this.expireDate = expireDate;
        this.enabled = true;
    }

    MsgTemplateEntity(String id, ResultSet res) {
        super(id, res);
        try {
            String _useScopes = ResultSetUtil.getString(res, "useScopes");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(_useScopes), "useScopes can't be null...");
            this.useScopes = Sets.newHashSet();
            Stream.of(StringUtils.split(_useScopes, ',')).forEach(x ->
                    this.useScopes.add(TemplateUseScope.paras(Integer.valueOf(x))));
            this.template = ResultSetUtil.getString(res, "template");
            String _classifies = ResultSetUtil.getString(res, "classifies");
            this.classifies = Sets.newHashSet();
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(classifies), "模板分类不可为空...");
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(useScopes), "模板使用场景不可为空...");
            this.classifies.addAll(Arrays.asList(StringUtils.split(_classifies, ',')));
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.orgId = ResultSetUtil.getObject(res, "orgId", Integer.class);
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
            this.expireDate = ResultSetUtil.getOptObject(res, "expireDate", Date.class).isPresent() ?
                    LocalDate.fromDateFields(ResultSetUtil.getOptObject(res, "expireDate", Date.class).get()) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Restore MsgTemplateEntity has SQLException", e);
        }
    }

    public String getTemplate() {
        return template;
    }

    public boolean isExpire() {
        return expireDate != null && LocalDate.now().isAfter(expireDate);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Set<String> getClassifies() {
        return classifies;
    }

    public Set<TemplateUseScope> getUseScopes() {
        return useScopes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MsgTemplateEntity)) return false;
        MsgTemplateEntity that = (MsgTemplateEntity) o;
        return enabled == that.enabled &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(orgId, that.orgId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(template, that.template) &&
                SetUtils.isEqualSet(classifies, that.classifies) &&
                SetUtils.isEqualSet(useScopes, that.useScopes) &&
                Objects.equal(expireDate, that.expireDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, orgId, storeId, template, classifies, useScopes, expireDate, enabled);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("orgId", orgId)
                .add("storeId", storeId)
                .add("template", template)
                .add("classifies", classifies)
                .add("useScopes", useScopes)
                .add("expireDate", expireDate)
                .add("enabled", enabled)
                .toString();
    }
}
