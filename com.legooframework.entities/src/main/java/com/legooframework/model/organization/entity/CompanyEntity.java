package com.legooframework.model.organization.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 公司只能被管理员 初始化，无法自我创建
 */
public class CompanyEntity extends BaseOrganization {

    private String[] storeIds = new String[0];

    CompanyEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.legalPerson = ResultSetUtil.getOptString(res, "comLegalPerson", null);
            this.orgCode = ResultSetUtil.getString(res, "orgCode");
            this.detailAddress = ResultSetUtil.getOptString(res, "comDetailAddress", null);
            this.businessLicense = ResultSetUtil.getOptString(res, "comBusinessLicense", null);
            this.shortName = ResultSetUtil.getOptString(res, "comShortName", null);
            this.fullName = ResultSetUtil.getOptString(res, "comFullName", null);
            this.fullName = ResultSetUtil.getOptString(res, "comFullName", null);
            this.contactNumber = ResultSetUtil.getOptString(res, "comContactNumber", null);
            this.remark = ResultSetUtil.getOptString(res, "comRemark", null);
            Optional<String> store_ids = ResultSetUtil.getOptObject(res, "storeIds", String.class);
            store_ids.ifPresent(x -> this.storeIds = StringUtils.split(x, ','));
        } catch (SQLException e) {
            throw new RuntimeException("Restore CompanyEntity has SQLException", e);
        }
    }

    public Optional<CompanyEntity> edit(String shortName, String businessLicense, String detailAddress,
                                        String legalPerson, String contactNumber, String remark) {
        LoginContextHolder.get();
        CompanyEntity clone = (CompanyEntity) super.cloneMe();
        clone.shortName = shortName;
        clone.businessLicense = businessLicense;
        clone.detailAddress = detailAddress;
        clone.legalPerson = legalPerson;
        clone.contactNumber = contactNumber;
        clone.remark = remark;
        if(this.equals4Edit(clone)) return Optional.empty();
        return Optional.of(clone);
    }

    boolean equals4Edit(Object o) {
        if(this == o) return true;
        if(!(o instanceof BaseOrganization)) return false;
        BaseOrganization that = (BaseOrganization) o;
        return Objects.equal(shortName, that.shortName) &&
                Objects.equal(orgCode, that.orgCode) &&
                Objects.equal(businessLicense, that.businessLicense) &&
                Objects.equal(detailAddress, that.detailAddress) &&
                Objects.equal(legalPerson, that.legalPerson) &&
                Objects.equal(contactNumber, that.contactNumber) &&
                Objects.equal(remark, that.remark);
    }

    /**
     * 给门店分配职员
     * @param store 门店
     * @param emps 职员
     * @return
     */
    public List<EmployeeEntity> assginEmployees(StoreEntity store,Collection<? extends EmployeeEntity> emps){
    	Preconditions.checkNotNull(store);
    	Preconditions.checkNotNull(emps);
    	return emps.stream().map(x -> {
    		x.setStoreId(store.getId());
    		return x;}).collect(Collectors.toList());
    }

    boolean contains(StoreEntity store) {
        if(ArrayUtils.isEmpty(this.storeIds)) return false;
        return ArrayUtils.contains(this.storeIds, String.valueOf(store.getId()));
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
                .add("stores's size", ArrayUtils.isEmpty(storeIds) ? 0 : storeIds.length)
                .toString();
    }
}
