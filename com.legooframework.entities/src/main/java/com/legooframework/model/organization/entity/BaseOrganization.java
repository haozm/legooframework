package com.legooframework.model.organization.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

abstract class BaseOrganization extends BaseEntity<Long> {

    protected String orgCode; // 机构编码
    protected String fullName; // 机构全称
    protected String shortName; // 机构简称
    protected String businessLicense; // 营业执照编码
    protected String detailAddress; // 详细地址
    protected String legalPerson; //  企业法人
    protected String contactNumber; // 联系电话
    protected String remark; // 备注
    private int type; // 机构类型 0 公司 / 1 门店

    BaseOrganization(Long orgId,String orgCode, Long tenantId, Long creator, String fullName, String shortName,
                     String businessLicense, String detailAddress, String legalPerson,
                     String contactNumber, String remark, int type) {
        super(orgId, tenantId, creator);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fullName), "机构全名不可以空.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(orgCode), "机构编码不可以空.");
        this.orgCode = orgCode;
        this.fullName = fullName;
        Preconditions.checkArgument(fullName.length() <= 128, "机构全称长度应小于128个字符.");
        this.remark = remark;
        this.shortName = shortName;
        if (!Strings.isNullOrEmpty(shortName))
            Preconditions.checkArgument(shortName.length() <= 128, "机构简称呼长度应小于128个字符.");
        this.businessLicense = businessLicense;
        if (!Strings.isNullOrEmpty(detailAddress))
            Preconditions.checkArgument(detailAddress.length() <= 128, "机构地址长度应小于128个字符.");
        this.detailAddress = detailAddress;
        if (!Strings.isNullOrEmpty(legalPerson))
            Preconditions.checkArgument(legalPerson.length() <= 32, "机构法人长度应小于128个字符.");
        this.legalPerson = legalPerson;
        this.contactNumber = contactNumber;
        this.type = type;
    }

    BaseOrganization(Long orgId, ResultSet res) {
        super(orgId, res);
        try {
            this.type = res.getInt("orgType");
        } catch (SQLException e) {
            throw new RuntimeException("Restore BaseOrganization's orgType has SQLException", e);
        }
    }

    public String getOrgCode() {
        return orgCode;
    }

    public String getFullName() {
        return fullName;
    }

    public Optional<String> getShortName() {
        return Optional.ofNullable(shortName);
    }

    public Optional<String> getBusinessLicense() {
        return Optional.ofNullable(businessLicense);
    }

    public Optional<String> getDetailAddress() {
        return Optional.ofNullable(detailAddress);
    }

    public String getLegalPerson() {
        return legalPerson;
    }

    public Optional<String> getContactNumber() {
        return Optional.ofNullable(contactNumber);
    }

    public Optional<String> getRemark() {
        return Optional.ofNullable(remark);
    }

    public boolean isCompany() {
        return 0 == this.type;
    }

    public boolean isStore() {
        return 1 == this.type;
    }

    @Override
    protected boolean equalsEntity(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseOrganization)) return false;
        BaseOrganization that = (BaseOrganization) o;
        return type == that.type &&
                Objects.equal(businessLicense, that.businessLicense);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> paramMap = super.toParamMap(excludes);
        paramMap.put("type", this.type);
        return paramMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseOrganization)) return false;
        if (!super.equals(o)) return false;
        BaseOrganization that = (BaseOrganization) o;
        return type == that.type &&
                Objects.equal(fullName, that.fullName) &&
                Objects.equal(orgCode, that.orgCode) &&
                Objects.equal(shortName, that.shortName) &&
                Objects.equal(businessLicense, that.businessLicense) &&
                Objects.equal(detailAddress, that.detailAddress) &&
                Objects.equal(legalPerson, that.legalPerson) &&
                Objects.equal(contactNumber, that.contactNumber) &&
                Objects.equal(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), orgCode,fullName, shortName, businessLicense, detailAddress,
                legalPerson, contactNumber, remark, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("orgCode", orgCode)
                .add("fullName", fullName)
                .add("shortName", shortName)
                .add("businessLicense", businessLicense)
                .add("detailAddress", detailAddress)
                .add("legalPerson", legalPerson)
                .add("contactNumber", contactNumber)
                .add("remark", remark)
                .add("type", type)
                .add("base", super.toString())
                .toString();
    }
}
