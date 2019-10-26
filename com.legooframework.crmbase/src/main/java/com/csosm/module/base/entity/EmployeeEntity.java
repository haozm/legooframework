package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.Replaceable;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeEntity extends BaseEntity<Integer> implements Replaceable {

    private final Integer loginUserId;
    // 1  启用  2 停用
    private final static int EMPLOYEE_STATE_ON = 1;
    private final static int EMPLOYEE_STATE_OFF = 2;
    public final static String DEFAULT_PASSWORD = "88888888";
    //0 删除  1存在
    private Integer state;

    private Integer employeeState;
    // 1 - 注册人， 2、其他员工
    private Integer employeeType;
    private String phoneNo, remark;
    private String userName;
    private Date birthday;
    private final Integer companyId;
    private final Integer organizationId;
    private Integer storeId;
    private Collection<Integer> roleIds;
    private final String loginName;
    private String passowrd;
    // 2 女 1 男
    private int sex;
    //迁移前职员ID
    private String oldEmployeeId;

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> params = super.toMap();
        params.put("loginUserId", loginUserId);
        params.put("password", passowrd);
        params.put("employeeState", employeeState);
        params.put("employeeType", employeeType);
        params.put("phoneNo", phoneNo);
        params.put("userName", userName);
        params.put("companyId", companyId);
        params.put("organizationId", organizationId);
        params.put("storeId", storeId);
        params.put("sex", sex);
        params.put("state", state);
        params.put("remark", remark);
        params.put("birthday", birthday);
        params.put("loginName", loginName);
        params.put("oldEmployeeId", this.oldEmployeeId);
        params.put("roleIds", roleIds != null ? Joiner.on(",").join(roleIds) : "");
        return params;
    }

    public static EmployeeEntity anonymous() {
        return new EmployeeEntity(-1, 1, 2, "匿名", null, null, "****", null, null, null, 1, null, null, 1, null,"");
    }

    EmployeeEntity(Integer id, Integer employeeState, Integer employeeType, String userName,
                   Integer companyId, Integer storeId, String passowrd, Integer organizationId, String loginName,
                   String phoneNo, int sex, String remark, Date birthday, int state,
                   Collection<Integer> roleIds,String oldEmployeeId) {
        super(id);
        this.loginUserId = -1;
        this.state = state;
        setPassowrd(passowrd);
        this.remark = remark;
        this.employeeState = employeeState;
        setUserName(userName);
        this.birthday = birthday;
        this.phoneNo = phoneNo;
        this.companyId = companyId;
        this.organizationId = organizationId;
        this.storeId = storeId;
        setEmployeeType(employeeType);
        this.sex = sex;
        this.loginName = loginName;
        this.roleIds = CollectionUtils.isEmpty(roleIds) ? null : Sets.newHashSet(roleIds);
        this.oldEmployeeId = oldEmployeeId;
    }

    static EmployeeEntity addStoreEmployee(String userName, String passowrd, String loginName, String phoneNo,
                                           Integer sex, String remarke, Date birthday, StoreEntity store,
                                           Collection<RoleEntity> roles, LoginUserContext loginUser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userName), "新增用户姓名不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "新增用户登陆账号不可以为空...");
        Preconditions.checkNotNull(store, "新增用户所属门店不可以为空...");
        EmployeeEntity employeeEntity = new EmployeeEntity(-1, 1, 2, userName,
                store.getCompanyId().or(-1), store.getId(), passowrd, null, loginName,
                phoneNo, sex, remarke, birthday, EMPLOYEE_STATE_ON,
                roles.stream().map(BaseEntity::getId).collect(Collectors.toList()),null);
        employeeEntity.setCreateUserId(loginUser.getUserId());
        return employeeEntity;
    }

    /**
     * 新增公司注册人
     *
     * @param passowrd
     * @param loginName
     * @param company
     * @param loginUser
     * @return
     */
    static EmployeeEntity addAdminEmployee(String passowrd, String loginName,String userName,String phoneNo,OrganizationEntity company,
                                           LoginUserContext loginUser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "新增用户登陆账号不可以为空...");
        Preconditions.checkNotNull(company, "注册人所属公司不可以为空...");
        EmployeeEntity employeeEntity = new EmployeeEntity(-1, 1, 2, userName,
                company.getMyCompanyId(), null, passowrd, null, loginName,
                phoneNo, 1, "公司注册人", null, EMPLOYEE_STATE_ON, Lists.newArrayList(1),null);
        employeeEntity.setCreateUserId(loginUser.getUserId());
        return employeeEntity;
    }

    static EmployeeEntity addOrgEmployee(String userName, String passowrd, String loginName, String phoneNo,
                                         Integer sex, String remarke, Date birthday, OrganizationEntity org,
                                         Collection<RoleEntity> roles, LoginUserContext loginUser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userName), "新增用户姓名不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "新增用户登陆账号不可以为空...");
        EmployeeEntity employeeEntity = new EmployeeEntity(-1, 1, 2, userName,
                loginUser.getCompany().get().getId(),
                null, passowrd, org == null ? -1 : org.getId(), loginName,
                phoneNo, sex, remarke, birthday, EMPLOYEE_STATE_ON,
                roles.stream().map(BaseEntity::getId).collect(Collectors.toList()),null);
        employeeEntity.setCreateUserId(loginUser.getUserId());
        return employeeEntity;
    }


    java.util.Optional<EmployeeEntity> enabled() {
        if (isEnabled()) return java.util.Optional.empty();
        try {
            EmployeeEntity clone = (EmployeeEntity) clone();
            clone.employeeState = EMPLOYEE_STATE_ON;
            return java.util.Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    java.util.Optional<EmployeeEntity> resetPassword(String pwd) {
        try {
            if (this.passowrd.equals(pwd)) return java.util.Optional.empty();
            EmployeeEntity clone = (EmployeeEntity) clone();
            clone.passowrd = pwd;
            return java.util.Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    java.util.Optional<EmployeeEntity> disabled() {
        if (!isEnabled()) return java.util.Optional.empty();
        try {
            EmployeeEntity clone = (EmployeeEntity) clone();
            clone.employeeState = EMPLOYEE_STATE_OFF;
            return java.util.Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改基本信息信息
     *
     * @param userName 名称
     * @param phoneNo  电话
     * @param sex      性别
     * @param roles    角色
     * @return
     */
    Optional<EmployeeEntity> modify(String userName, String phoneNo, int sex, Collection<RoleEntity> roles) {
        EmployeeEntity clone = null;
        try {
            clone = (EmployeeEntity) this.clone();
            clone.userName = userName;
            clone.phoneNo = phoneNo;
            clone.sex = sex;
            clone.roleIds = Lists.newArrayList();
            clone.roleIds.addAll(roles.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            if (this.equals(clone)) return Optional.absent();
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("复制职员实体异常");
        }
    }

    boolean isLoginName(String loginName) {
        return this.loginName.equals(loginName);
    }

    /**
     * 修改密码
     *
     * @param newPwd 新密码密码
     * @return
     */
    public Optional<EmployeeEntity> changePassword(String oldPwd, String newPwd) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newPwd), "新密码不可以为空值...");
        Preconditions.checkState(this.passowrd.equals(oldPwd), "原密码错误...");
        if (oldPwd.equals(newPwd)) return Optional.absent();
        EmployeeEntity clone = null;
        try {
            clone = (EmployeeEntity) this.clone();
            clone.passowrd = newPwd;
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("复制职员实体异常");
        }
    }
    
    /**
     * 迁移门店
     * @param store
     * @return
     */
    public EmployeeEntity switchStore(StoreEntity store) {
    	Preconditions.checkNotNull(store, "门店不能为空");
    	Preconditions.checkArgument(store.getCompanyId().isPresent(), 
    			String.format("门店[%s]无公司信息", store.getId()));
    	Preconditions.checkState(this.companyId.intValue() == 
    			store.getCompanyId().get().intValue(), "不允许跨公司迁移职员");
    	this.employeeState = EMPLOYEE_STATE_OFF;
    	EmployeeEntity clone = null;
    	try {
			clone = (EmployeeEntity) this.clone();
			clone.storeId = store.getId();
			clone.oldEmployeeId = this.getId().toString();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
    	return clone;
    }
    
    public boolean hasOldEmployee() {
    	return !Strings.isNullOrEmpty(oldEmployeeId);
    }
    
    public boolean isOldEmployee(EmployeeEntity employee) {
    	return !Strings.isNullOrEmpty(this.oldEmployeeId)
    			&&Integer.parseInt(this.oldEmployeeId) == employee.getId();
    }
    
    public void setOldEmployee(EmployeeEntity employee) {
    	this.oldEmployeeId = employee.getId().toString();
    }
    
    public int getSex() {
        return sex;
    }

    public boolean isAdmin() {
        return StringUtils.equals("admin", this.userName);
    }

    public boolean isCompanyAdmin() {
        return Objects.equal(this.companyId, this.organizationId);
    }

    public boolean hasDianzhang() {
        return getRoleIds().isPresent() && getRoleIds().get().contains(5);
    }

    public boolean hasDaogou() {
        return getRoleIds().isPresent() && getRoleIds().get().contains(7);
    }

    public Optional<Collection<Integer>> getRoleIds() {
        return Optional.fromNullable(roleIds);
    }

    public boolean checkPwd(String passowrd) {
        return StringUtils.equalsIgnoreCase(this.passowrd, passowrd);
    }

    private void setEmployeeState(Integer employeeState) {
        this.employeeState = employeeState;
    }

    public String getLoginName() {
        return loginName;
    }

    public Optional<Integer> getOrganizationId() {
        return Optional.fromNullable(organizationId);
    }

    @Deprecated
    public Optional<Integer> getCompanyId() {
        return Optional.fromNullable(companyId);
    }
    
    public Integer getExistCompanyId() {
    	return this.companyId;
    }
    
    public boolean isEnabled() {
        return EMPLOYEE_STATE_ON == this.employeeState;
    }

    public Integer getLoginUserId() {
        return loginUserId;
    }

    public String getPassowrd() {
        return passowrd;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getUserName() {
        return userName;
    }

    public Optional<Integer> getStoreId() {
        return Optional.fromNullable(storeId);
    }

    private void setUserName(String userName) {
        this.userName = Strings.isNullOrEmpty(userName) ? getId().toString() : userName;
    }

    boolean isCompanyEmp() {
        return this.organizationId == null && this.storeId == null;
    }

    boolean isOrgEmp(OrganizationEntity org) {
        return this.organizationId != null && this.organizationId.equals(org.getId());
    }

    boolean isStoreEmp(StoreEntity store) {
        return this.storeId != null && this.storeId.equals(store.getId());
    }

    private void setPassowrd(String passowrd) {
        this.passowrd = passowrd;
    }

    private void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    private void setEmployeeType(Integer employeeType) {
        this.employeeType = employeeType;
    }

    public String getRemark() {
        return remark;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setModifyUser(LoginUserContext loginUser) {
        this.setCreateUserId(loginUser.getUserId());
    }

    public String getOldEmployeeId() {
    	return this.oldEmployeeId;
    }
    
    public void setCreateUser(LoginUserContext user) {
    	this.setCreateUserId(user.getUserId());
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("loginUserId", loginUserId)
                .add("passowrd", passowrd)
                .add("employeeState", employeeState)
                .add("employeeType", employeeType)
                .add("organizationId", organizationId)
                .add("phoneNo", phoneNo)
                .add("userName", userName)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("state", state)
                .add("roleIds", roleIds)
                .toString();
    }

    public OrgTreeViewDto buildOrgTreeDto() {
        return new OrgTreeViewDto(this);
    }

    @Override
    public Map<String, String> toSmsMap(StoreEntity store) {
        Map<String, String> map = Maps.newHashMap();
        map.put("{职员姓名}", this.userName == null ? "" : this.userName);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EmployeeEntity that = (EmployeeEntity) o;
        return Objects.equal(loginUserId, that.loginUserId)
                && Objects.equal(passowrd, that.passowrd)
                && Objects.equal(employeeState, that.employeeState)
                && Objects.equal(employeeType, that.employeeType)
                && Objects.equal(phoneNo, that.phoneNo)
                && Objects.equal(sex, that.sex)
                && Objects.equal(loginName, that.loginName)
                && Objects.equal(userName, that.userName)
                && Objects.equal(organizationId, that.organizationId)
                && Objects.equal(storeId, that.storeId)
                && Objects.equal(state, that.state)
                && Objects.equal(companyId, that.companyId)
                && org.apache.commons.collections4.CollectionUtils.isEqualCollection(roleIds, that.roleIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), loginUserId, passowrd, employeeState,
                employeeType, phoneNo, organizationId, userName, state, storeId, companyId);
    }
}
