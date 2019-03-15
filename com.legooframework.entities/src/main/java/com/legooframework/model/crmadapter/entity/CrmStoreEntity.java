package com.legooframework.model.crmadapter.entity;


import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.util.Objects;

public class CrmStoreEntity extends BaseEntity<Integer> {

    private String name;
    private String orgCode;
    private Integer companyId, orgId;

    CrmStoreEntity(Integer id, String name, String orgCode, Integer orgId, Integer companyId) {
        super(id);
        this.name = name;
        this.orgCode = orgCode;
        this.orgId = orgId;
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    String getOrgCode() {
        return orgCode;
    }

    public boolean isOwnerOrg(CrmOrganizationEntity organization) {
        return Objects.equals(this.orgId, organization.getId());
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isCrossStore(CrmMemberEntity member) {
        return !member.getStoreId().equals(this.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrmStoreEntity)) return false;
        if (!super.equals(o)) return false;
        CrmStoreEntity that = (CrmStoreEntity) o;
        return com.google.common.base.Objects.equal(name, that.name) &&
                com.google.common.base.Objects.equal(orgCode, that.orgCode) &&
                com.google.common.base.Objects.equal(orgId, that.orgId) &&
                com.google.common.base.Objects.equal(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(super.hashCode(), name, orgCode, orgId, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("orgCode", orgCode)
                .add("orgId", orgId)
                .add("companyId", companyId)
                .toString();
    }
}
