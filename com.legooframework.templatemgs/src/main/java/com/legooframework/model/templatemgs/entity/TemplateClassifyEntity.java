package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TemplateClassifyEntity extends BaseEntity<String> {

    private final String pId;
    private final String classify;
    private final String deepPath;
    private Integer companyId;

    TemplateClassifyEntity(String id, TemplateClassifyEntity parent, String classify, CrmOrganizationEntity company) {
        super(id, company.getCompanyId().longValue(), -1L);
        this.pId = parent.getId();
        this.classify = classify;
        this.deepPath = String.format("%s-%s", parent.getDeepPath(), this.getId());
        this.companyId = company.getCompanyId();
    }

    TemplateClassifyEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.pId = ResultSetUtil.getString(res, "pId");
            this.classify = ResultSetUtil.getString(res, "classify");
            this.deepPath = ResultSetUtil.getString(res, "deepPath");
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
        } catch (SQLException e) {
            throw new RuntimeException("Restore TemplateClassifyEntity has SQLException", e);
        }
    }

    String getDeepPath() {
        return deepPath;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("deepPath", this.deepPath);
        return params;
    }

    TreeNode treeNode() {
        return new TreeNode(getId(), this.pId, this.classify, this.toParamMap());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateClassifyEntity)) return false;
        if (!super.equals(o)) return false;
        TemplateClassifyEntity that = (TemplateClassifyEntity) o;
        return Objects.equal(pId, that.pId) &&
                Objects.equal(classify, that.classify) &&
                Objects.equal(deepPath, that.deepPath) &&
                Objects.equal(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), pId, classify, deepPath, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("pId", pId)
                .add("companyId", companyId)
                .add("classify", classify)
                .add("deepPath", deepPath)
                .toString();
    }
}
