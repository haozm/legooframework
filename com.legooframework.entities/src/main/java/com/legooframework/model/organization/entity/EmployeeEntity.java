package com.legooframework.model.organization.entity;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.security.entity.AccountEntity;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class EmployeeEntity extends BaseEntity<Long> {
    private String workNo;//职员工作号
    private String userName;//职员名称
    private String userSex;//职员性别
    private String userSexName;//职员性别名称
    private Date userBirthday;//职员生日
    private String userRemark;//职员备注
    private String comWorkStatus;//职员工作状态
    private String comWorkStatusName;//职员工作状态名称
    private String phoneNo;//职员电话
    private Date employeeTime;// 职员入职时间
    private String placeId;// 职员籍贯
    private String location;// 职员详细地址
    private Long accountId, orgId, storeId, companyId;//账号ID、组织ID、门店ID、公司ID
    public static final String WORKSTATUS_DICT = "WORKSTATUS";
    public static final String SEX_DICT = "SEX";

    // 新增职员 默认在职
    EmployeeEntity(Long userId, String workNo, String userName, KvDictDto userSex, Date userBirthday, String userRemark,
                   String phoneNo, Date employeeTime, Long orgId, Long storeId, LoginContext lc) {
        super(userId, lc.getTenantId(), lc.getLoginId());
        if (!Strings.isNullOrEmpty(userName))
            Preconditions.checkArgument(userName.length() <= 32, "职员姓名长度最大为32个字符");
        this.userName = userName;
        this.userSex = userSex == null ? "0" : userSex.getValue();
        this.userBirthday = userBirthday;
        if (!Strings.isNullOrEmpty(userRemark))
            Preconditions.checkArgument(userRemark.length() <= 256, "职员备注长度最大为32个字符");
        this.userRemark = userRemark;
        if (!Strings.isNullOrEmpty(workNo))
            Preconditions.checkArgument(workNo.length() <= 32, "职员工号长度最大为32个字符");
        this.workNo = workNo;
        this.comWorkStatus = "1";
        this.phoneNo = phoneNo;
        this.employeeTime = employeeTime;
        this.orgId = orgId;
        this.companyId = lc.getTenantId();
        this.storeId = storeId;
    }

    public boolean isWorking() {
        return StringUtils.equalsAny(comWorkStatus, "1", "2");
    }

    public boolean isInService() {
        return StringUtils.equals(comWorkStatus, "1");
    }

    public boolean isInVacation() {
        return StringUtils.equals(comWorkStatus, "2");
    }

    public boolean isQuited() {
        return StringUtils.equals(comWorkStatus, "0");
    }

    /**
     * 在职
     *
     * @return
     */
    public EmployeeEntity doInService() {
        EmployeeEntity clone = (EmployeeEntity) this.cloneMe();
        clone.comWorkStatus = "1";
        return clone;
    }

    /**
     * 休假状态
     *
     * @return
     */
    public EmployeeEntity doInVacation() {
        EmployeeEntity clone = (EmployeeEntity) this.cloneMe();
        clone.comWorkStatus = "2";
        return clone;
    }

    /**
     * 离职状态
     *
     * @return
     */
    public EmployeeEntity doQuit() {
        EmployeeEntity clone = (EmployeeEntity) this.cloneMe();
        clone.comWorkStatus = "0";
        return clone;
    }

    EmployeeEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.userName = ResultSetUtil.getOptString(res, "userName", null);
            this.userSex = ResultSetUtil.getOptString(res, "userSex", null);
            this.accountId = ResultSetUtil.getOptObject(res, "accountId", Long.class).orElse(null);
            this.userBirthday = res.getDate("userBirthday");
            this.employeeTime = res.getDate("employeeTime");
            this.userRemark = ResultSetUtil.getOptString(res, "userRemark", null);
            this.workNo = ResultSetUtil.getOptString(res, "workNo", null);
            this.comWorkStatus = ResultSetUtil.getOptString(res, "comWorkStatus", null);
            this.comWorkStatusName = ResultSetUtil.getOptString(res, "comWorkStatusName", null);
            this.userSexName = ResultSetUtil.getOptString(res, "userSexName", null);
            this.phoneNo = ResultSetUtil.getOptString(res, "phoneNo", null);
            this.orgId = ResultSetUtil.getOptObject(res, "orgId", Long.class).orElse(null);
            this.companyId = ResultSetUtil.getOptObject(res, "companyId", Long.class).orElse(null);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Long.class).orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore EmployeeEntity has SQLException", e);
        }
    }

    public EmployeeEntity modify(String userName, Date userBirthday, String userRemark, String phoneNo, Date employeeTime) {
        EmployeeEntity clone = (EmployeeEntity) this.cloneMe();
        clone.userName = userName;
        clone.userBirthday = userBirthday;
        clone.userRemark = userRemark;
        clone.phoneNo = phoneNo;
        clone.employeeTime = employeeTime;
        return clone;
    }

    public Optional<Long> getAccountId() {
        return Optional.ofNullable(accountId);
    }

    public boolean isBindAccount() {
        return this.accountId != null;
    }

    public EmployeeEntity bindAccount(AccountEntity account) {
        if (isBindAccount() && Objects.equal(this.accountId, account.getId()))
            return this;
        Preconditions.checkState(!isBindAccount(), "当前职员已经绑定账号，不允许多次绑定.");
        EmployeeEntity clone = (EmployeeEntity) super.cloneMe();
        clone.setAccountId(account.getId());
        return clone;
    }

    public boolean hasStore() {
        return this.storeId != null;
    }

    public Optional<Long> getStoreId() {
        return Optional.ofNullable(storeId);
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    private void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserSex() {
        return userSex;
    }

    public Date getUserBirthday() {
        return userBirthday;
    }

    public String getUserRemark() {
        return userRemark;
    }

    public String getWorkNo() {
        return workNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Date getEmployeeTime() {
        return employeeTime;
    }

    public void setEmployeeTime(Date employeeTime) {
        this.employeeTime = employeeTime;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getComWorkStatus() {
        return comWorkStatus;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> map = super.toParamMap(excludes);
        map.put("comWorkStatus", comWorkStatus);
        return map;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((comWorkStatus == null) ? 0 : comWorkStatus.hashCode());
        result = prime * result + ((comWorkStatusName == null) ? 0 : comWorkStatusName.hashCode());
        result = prime * result + ((companyId == null) ? 0 : companyId.hashCode());
        result = prime * result + ((employeeTime == null) ? 0 : employeeTime.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
        result = prime * result + ((phoneNo == null) ? 0 : phoneNo.hashCode());
        result = prime * result + ((placeId == null) ? 0 : placeId.hashCode());
        result = prime * result + ((storeId == null) ? 0 : storeId.hashCode());
        result = prime * result + ((userBirthday == null) ? 0 : userBirthday.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((userRemark == null) ? 0 : userRemark.hashCode());
        result = prime * result + ((userSex == null) ? 0 : userSex.hashCode());
        result = prime * result + ((userSexName == null) ? 0 : userSexName.hashCode());
        result = prime * result + ((workNo == null) ? 0 : workNo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EmployeeEntity other = (EmployeeEntity) obj;
        if (accountId == null) {
            if (other.accountId != null)
                return false;
        } else if (!accountId.equals(other.accountId))
            return false;
        if (comWorkStatus == null) {
            if (other.comWorkStatus != null)
                return false;
        } else if (!comWorkStatus.equals(other.comWorkStatus))
            return false;
        if (comWorkStatusName == null) {
            if (other.comWorkStatusName != null)
                return false;
        } else if (!comWorkStatusName.equals(other.comWorkStatusName))
            return false;
        if (companyId == null) {
            if (other.companyId != null)
                return false;
        } else if (!companyId.equals(other.companyId))
            return false;
        if (employeeTime == null) {
            if (other.employeeTime != null)
                return false;
        } else if (!employeeTime.equals(other.employeeTime))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (orgId == null) {
            if (other.orgId != null)
                return false;
        } else if (!orgId.equals(other.orgId))
            return false;
        if (phoneNo == null) {
            if (other.phoneNo != null)
                return false;
        } else if (!phoneNo.equals(other.phoneNo))
            return false;
        if (placeId == null) {
            if (other.placeId != null)
                return false;
        } else if (!placeId.equals(other.placeId))
            return false;
        if (storeId == null) {
            if (other.storeId != null)
                return false;
        } else if (!storeId.equals(other.storeId))
            return false;
        if (userBirthday == null) {
            if (other.userBirthday != null)
                return false;
        } else if (!userBirthday.equals(other.userBirthday))
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        if (userRemark == null) {
            if (other.userRemark != null)
                return false;
        } else if (!userRemark.equals(other.userRemark))
            return false;
        if (userSex == null) {
            if (other.userSex != null)
                return false;
        } else if (!userSex.equals(other.userSex))
            return false;
        if (userSexName == null) {
            if (other.userSexName != null)
                return false;
        } else if (!userSexName.equals(other.userSexName))
            return false;
        if (workNo == null) {
            if (other.workNo != null)
                return false;
        } else if (!workNo.equals(other.workNo))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EmployeeEntity [workNo=" + workNo + ", userName=" + userName + ", userSex=" + userSex + ", userSexName="
                + userSexName + ", userBirthday=" + userBirthday + ", userRemark=" + userRemark + ", comWorkStatus="
                + comWorkStatus + ", comWorkStatusName=" + comWorkStatusName + ", phoneNo=" + phoneNo
                + ", employeeTime=" + employeeTime + ", placeId=" + placeId + ", location=" + location + ", accountId="
                + accountId + ", orgId=" + orgId + ", storeId=" + storeId + ", companyId=" + companyId + "]";
    }
}
