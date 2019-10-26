package com.csosm.module.base.entity;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.Replaceable;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class StoreEntity extends BaseEntity<Integer> implements Replaceable {

    private Integer type;
    private String name, address, oldStoreId;
    private List<String> deviceIds;

    private Integer status;
    private Integer organizationId;
    private Integer companyId;
    private String qrCode;
    private int birthdayBefore;
    private int beforeDays = -1;
    //门店电话号码
    private String phone;
    // 手机号隐藏（0：隐藏；1：不隐藏）
    private Integer hiddenMemberPhoneFlag = 1;

    //门店启用状态 1 为启用 2为禁用
    private Integer state;
    
    //RFM设置 1为累计 2全年
    private Integer rfmSetting;
    
    public StoreEntity(Integer id, int type, String name, String address, String oldStoreId,
                       int status, Integer organizationId, Integer companyId, String deviceIds,
                       String qrCode, int birthdayBefore, int beforeDays,
                       String phone, Integer hiddenMemberPhoneFlag, Integer state,
                       Integer rfmSetting) {
        super(id);
        this.type = type;
        this.name = name;
        this.address = address;
        this.status = status;
        this.oldStoreId = oldStoreId;
        this.organizationId = organizationId;
        this.companyId = companyId;
        this.status = status;
        this.deviceIds = Strings.isNullOrEmpty(deviceIds) ? null : Lists.newArrayList(deviceIds.split(","));
        this.qrCode = qrCode;
        this.birthdayBefore = birthdayBefore;
        this.beforeDays = beforeDays;
        this.phone = phone;
        this.hiddenMemberPhoneFlag = hiddenMemberPhoneFlag;
        this.state = state;
        this.rfmSetting = rfmSetting;
    }

    StoreEntity(OrganizationEntity company, OrganizationEntity parent, int type, String name,
                String address, String phone, int state) {
        super(null);
        Preconditions.checkNotNull(company, "入参company不能为空");
        Preconditions.checkNotNull(parent, "入参parent不能为空");
        Preconditions.checkArgument(Lists.newArrayList(1, 2, 3, 4).contains(type), "门店类型type不在【1,2,3,4】范围内");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
        Preconditions.checkArgument(Lists.newArrayList(1, 2).contains(state), "门店启用状态state不在【1,2】范围内");
        this.companyId = company.getId();
        this.organizationId = parent.getId();
        this.type = type;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.state = state;
    }

    public StoreEntity modify(int type, String name, String address, String phone, int state, int hiddenPhone) {
        Preconditions.checkArgument(Lists.newArrayList(1, 2, 3, 4).contains(type), "门店类型type不在【1,2,3,4】范围内");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
        Preconditions.checkArgument(Lists.newArrayList(1, 2).contains(state), "门店启用状态state不在【1,2】范围内");
        Preconditions.checkArgument(Lists.newArrayList(0, 1).contains(hiddenPhone), "隐藏会员电话hiddenPhone不在【0,1】范围内");
        StoreEntity clone = null;
        try {
            clone = (StoreEntity) this.clone();
            clone.type = type;
            clone.name = name;
            clone.address = address;
            clone.phone = phone;
            clone.state = state;
            clone.hiddenMemberPhoneFlag = hiddenPhone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("复制门店异常");
        }
        return clone;
    }


    Optional<StoreEntity> modifyBeforeDays(int beforeDays) {
        Preconditions.checkArgument(beforeDays > 0);
        if (this.beforeDays == beforeDays) return Optional.absent();
        try {
            StoreEntity clone = (StoreEntity) this.clone();
            clone.beforeDays = beforeDays;
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBeforeDays() {
        return beforeDays == -1 ? 2 : beforeDays;
    }

    Optional<StoreEntity> modifyBirthdayBefore(int birthdayBefore) {
        Preconditions.checkArgument(birthdayBefore > 0);
        if (this.birthdayBefore == birthdayBefore) return Optional.absent();
        try {
            StoreEntity clone = (StoreEntity) this.clone();
            clone.birthdayBefore = birthdayBefore;
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    Optional<StoreEntity> modifyQRcode(String qrCode) {
        if (Strings.isNullOrEmpty(qrCode)) return Optional.absent();
        if (StringUtils.equals(this.qrCode, qrCode)) return Optional.absent();
        try {
            StoreEntity clone = (StoreEntity) this.clone();
            clone.qrCode = qrCode;
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBirthdayBefore() {
        return birthdayBefore;
    }

    public Optional<String> getQrCode() {
        return Optional.fromNullable(qrCode);
    }

    public OrgTreeViewDto buildOrgTreeDto() {
        return new OrgTreeViewDto(this);
    }

    public boolean isDirectlyType() {
        return null != type && 1 == type;
    }

    public boolean isUnkonwType() {
        return null == type;
    }

    public boolean isJoinType() {
        return null != type && 2 == type;
    }

    public boolean isAgentType() {
        return null != type && 3 == type;
    }

    public boolean isOtherType() {
        return null != type && 3 == type;
    }

    public boolean isEnabled() {
        return null != status && 1 == this.status;
    }

    public boolean hasDevice() {
        return CollectionUtils.isNotEmpty(this.deviceIds);
    }

    public Optional<List<String>> getDeviceIds() {
        return Optional.fromNullable(this.deviceIds);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getOldStoreId() {
        return Optional.fromNullable(oldStoreId);
    }

    public boolean isStatusEnbaled() {
        return status == 1;
    }

    public String getAddress() {
        return address;
    }

    public void changeParent(OrganizationEntity org) {
        this.organizationId = org.getId();
    }

    public boolean isParent(OrganizationEntity org) {
        return org.getId() == this.organizationId;
    }

    public Optional<Integer> getOrganizationId() {
        return Optional.fromNullable(organizationId);
    }

    public Integer getExistCompanyId() {
        return this.companyId;
    }
    
    @Deprecated
    public Optional<Integer> getCompanyId() {
        return Optional.fromNullable(companyId);
    }
    
    public Integer getRfmSetting() {
		return rfmSetting;
	}
   
    
	public String getContactTableName() {
        Preconditions.checkState(this.getCompanyId().isPresent(), String.format("门店[%s] 无公司信息", this.getId()));
        if (hasDevice())
            return String.format("CONTACT_%s_%s", this.getCompanyId().get(), this.getId());
        return "CONTACT_MOULD";
    }

    public void setCreateUser(LoginUserContext loginUser) {
        this.setCreateUserId(loginUser.getUserId());
    }

    public void setModifyUser(LoginUserContext loginUser) {
        this.setModifyUserId(loginUser.getUserId());
    }

	@Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("name", this.name);
        map.put("address", this.address);
        map.put("companyId", this.companyId);
        map.put("qrCode", this.qrCode);
        map.put("birthdayBefore", this.birthdayBefore);
        map.put("phone", this.phone);
        map.put("state", this.state);
        map.put("type", this.type);
        map.put("organizationId", this.organizationId);
        map.put("hiddenMemberPhoneFlag", this.hiddenMemberPhoneFlag);
        map.put("beforeDays", this.beforeDays == -1 ? 2 : this.beforeDays);
        map.put("rfmSetting", this.rfmSetting);
        return map;
    }


    public Map<String, Object> toViewMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", this.getId());
        map.put("name", this.name);
        map.put("address", Strings.isNullOrEmpty(this.address) ? "" : this.address);
        map.put("companyId", this.companyId);
        map.put("qrCode", Strings.isNullOrEmpty(this.qrCode) ? "" : this.qrCode);
        map.put("birthdayBefore", this.birthdayBefore);
        map.put("phone", Strings.isNullOrEmpty(this.phone) ? "" : this.phone);
        map.put("state", this.state == null ? 1 : this.state);
        map.put("type", this.type == null ? 1 : this.type);
        map.put("organizationId", this.organizationId);
        map.put("hiddenMemberPhoneFlag", this.hiddenMemberPhoneFlag == null ? 1 : this.hiddenMemberPhoneFlag);
        map.put("beforeDays", this.beforeDays == -1 ? 2 : this.beforeDays);
        map.put("rfmSetting", this.rfmSetting);
        return map;
    }

    @Override
    public Map<String, String> toSmsMap(StoreEntity store) {
        Map<String, String> map = Maps.newHashMap();
        map.put("{门店名称}", this.name == null ? "" : this.name);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StoreEntity that = (StoreEntity) o;
        return Objects.equal(type, that.type)
                && Objects.equal(name, that.name)
                && Objects.equal(address, that.address)
                && Objects.equal(oldStoreId, that.oldStoreId)
                && Objects.equal(state, that.state)
                && Objects.equal(organizationId, that.organizationId)
                && Objects.equal(companyId, that.companyId)
                && SetUtils.isEqualSet(deviceIds, that.deviceIds)
                && Objects.equal(qrCode, that.qrCode)
                && Objects.equal(phone, that.phone)
                && Objects.equal(beforeDays, that.beforeDays)
                && this.birthdayBefore == that.birthdayBefore;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), type, name,
                address, oldStoreId, status, organizationId, companyId, deviceIds, qrCode, birthdayBefore, beforeDays);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("type", type)
                .add("name", name)
                .add("status", status)
                .add("deviceIds", CollectionUtils.isEmpty(deviceIds) ? "" : deviceIds.toString())
                .add("companyId", companyId)
                .add("oldStoreId", oldStoreId)
                .add("organizationId", organizationId)
                .add("address", address)
                .add("qrCode", qrCode)
                .add("birthdayBefore", birthdayBefore)
                .add("beforeDays", beforeDays)
                .toString();
    }
}
