package com.legooframework.model.crmadapter.entity;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;

public class CrmOrganizationEntity extends BaseEntity<Integer> {

    private final Integer companyId;
    private final String code;
    private final Integer type;  // 1 com  2 org
    private final String name;
    private final String shortName;

    CrmOrganizationEntity(Integer id, Integer companyId, String code, Integer type,
                          String name, String shortName) {
        super(id);
        this.companyId = companyId;
        this.code = code;
        this.type = type;
        this.name = name;
        this.shortName = shortName;
    }

    public boolean isSubOrg(CrmOrganizationEntity organization) {
        return StringUtils.startsWith(this.code, String.format("%s_", organization.code));
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isCompany() {
        return Objects.equal(1, type);
    }

    public boolean isOrg() {
        return Objects.equal(2, type);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrmOrganizationEntity that = (CrmOrganizationEntity) o;
        return Objects.equal(code, that.code)
                && Objects.equal(type, that.type)
                && Objects.equal(companyId, that.companyId)
                && Objects.equal(name, that.name)
                && Objects.equal(shortName, that.shortName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                super.hashCode(), code, type, name, companyId, shortName);
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("code", code)
                .add("type", type)
                .add("name", name)
                .add("shortName", shortName)
                .toString();
    }
}
