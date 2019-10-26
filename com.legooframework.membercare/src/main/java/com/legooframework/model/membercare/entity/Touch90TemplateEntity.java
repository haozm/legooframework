package com.legooframework.model.membercare.entity;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 90 模板
 */
public class Touch90TemplateEntity extends BaseEntity<String> implements BatchSetter {

    private Integer companyId;
    private String nodeId, nodeName, categories;
    private Set<Integer> storeIds;

    private static String generateId(Integer companyId, String categories, String nodeId) {
        return String.format("%s_%s_%s_%s", BusinessType.TOUCHED90.toString(), companyId, categories, nodeId);
    }

    Touch90TemplateEntity(String id, ResultSet res) {
        super(id, res);
        try {
            String store_ids = ResultSetUtil.getString(res, "storeIds");
            this.storeIds = Stream.of(StringUtils.split(store_ids, ',')).map(Integer::valueOf).collect(Collectors.toSet());
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.nodeId = ResultSetUtil.getObject(res, "templateId", String.class);
            this.categories = ResultSetUtil.getObject(res, "categories", String.class);
            this.nodeName = ResultSetUtil.getObject(res, "templateName", String.class);
        } catch (SQLException e) {
            throw new RuntimeException("Restore Touch90TemplateEntity has SQLException", e);
        }
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // id, company_id, template_id, template_name, store_ids,  tenant_id, creator
        ps.setObject(1, getId());
        ps.setObject(2, this.companyId);
        ps.setObject(3, this.nodeId);
        ps.setObject(4, this.nodeName);
        ps.setObject(5, StringUtils.join(this.storeIds, ','));
        ps.setObject(6, this.companyId);
        ps.setObject(7, this.getCreator());
        ps.setObject(8, this.categories);
    }

    String getNodeId() {
        return nodeId;
    }

    Touch90TemplateEntity(TaskCareDetailRule rule, String categories, LoginContext user) {
        super(generateId(user.getTenantId().intValue(), categories, rule.getId()), user.getTenantId(),
                user.getLoginId());
        this.companyId = user.getTenantId().intValue();
        this.storeIds = Sets.newHashSet();
        this.storeIds.add(-1);
        this.nodeId = rule.getId();
        this.categories = categories;
        setNodeName(rule);
    }

    boolean hasCompany() {
        return this.storeIds.contains(-1);
    }

    boolean hasStore(CrmStoreEntity store) {
        return this.storeIds.contains(store.getId());
    }

    public String getNodeName() {
        return nodeName;
    }

    boolean hasTemplate(TaskCareRule4Touch90Entity careRule, TaskCareDetailRule rule) {
        return StringUtils.equals(this.nodeId, rule.getId()) && StringUtils.equals(this.categories, careRule.getCategories());
    }

    private void setNodeName(TaskCareDetailRule rule) {
        if (Strings.isNullOrEmpty(this.nodeName)) {
            Duration delay = rule.getDelay();
            this.nodeName = String.format("90服务第%s节点", delay.toCnString());
        }
    }

    public String getCategories() {
        return categories;
    }

    void addByStore(CrmStoreEntity store) {
        this.storeIds.add(store.getId());
    }

    void addByCompany() {
        this.storeIds.add(-1);
    }

    Touch90TemplateEntity cloneSelf() {
        Touch90TemplateEntity clone = (Touch90TemplateEntity) cloneMe();
        clone.storeIds = Sets.newHashSet(this.storeIds);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Touch90TemplateEntity that = (Touch90TemplateEntity) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(nodeId, that.nodeId) &&
                Objects.equal(categories, that.categories) &&
                Objects.equal(nodeName, that.nodeName) &&
                Objects.equal(storeIds, that.storeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, nodeId, categories, nodeName, storeIds);
    }
}
