package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.*;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeServer extends AbstractBaseServer {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServer.class);

    public Optional<List<EmployeeEntity>> loadEnabledShoppingGuides(Integer storeId, LoginUserContext userContext) {
        Preconditions.checkNotNull(storeId, "门店ID不可以为空。");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(storeId);
        Preconditions.checkState(store.isPresent(), "不存在ID=%s对应的门店.");
        Preconditions.checkState(store.get().getCompanyId().isPresent(), "门店所属公司数据异常.");
        Preconditions.checkState(
                userContext.getCompany().isPresent()
                        && Objects.equal(userContext.getCompany().get().getId(), store.get().getCompanyId().get()),
                "门店与登陆用户公司不一致.");
        Optional<List<EmployeeEntity>> emploies = getBean(EmployeeEntityAction.class).loadEmployeesByStore(store.get(), null);
        if (!emploies.isPresent()) return Optional.absent();
        List<EmployeeEntity> shoppingGuides = emploies.get().stream().filter(EmployeeEntity::isEnabled).collect(Collectors.toList());
        return Optional.fromNullable(CollectionUtils.isEmpty(shoppingGuides) ? null : shoppingGuides);
    }

    /**
     * 新增组织职员信息
     *
     * @param loginUser
     * @param orgId
     * @param loginName
     * @param userName
     * @param phoneNo
     * @param sex
     * @param roleIds
     */
    public void saveOrgEmployee(LoginUserContext loginUser, Integer orgId, String loginName, String userName,
                                String phoneNo, int sex, Collection<Integer> roleIds) {
        Preconditions.checkNotNull(loginUser, "入参loginUser不能为空");
        Preconditions.checkNotNull(orgId, "入参orgId不能为空");
        OrganizationEntity org = getBean(OrganizationEntityAction.class).loadById(orgId);
        Preconditions.checkArgument(loginUser.getCompany().isPresent(), "登录用户无公司信息");
        List<RoleEntity> roles = getBean(RoleEntityAction.class).findByIds(loginUser.getCompany().get(), roleIds);
        checkOrgRole(loginUser);
        checkOrgEmpRoles(roles);
        getBean(EmployeeEntityAction.class).saveOrgEmployee(loginUser, org, loginName, userName, phoneNo, sex, roles);
    }

    /**
     * 增添门店职员
     *
     * @param loginUser
     * @param storeId
     * @param loginName
     * @param userName
     * @param phoneNo
     * @param sex
     * @param roleIds
     */
    public void saveStoreEmployee(LoginUserContext loginUser, Integer storeId, String loginName, String userName,
                                  String phoneNo, int sex, Collection<Integer> roleIds) {
        Preconditions.checkNotNull(loginUser, "入参loginUser不能为空");
        Preconditions.checkNotNull(storeId, "入参storeId不能为空");
        StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
        Preconditions.checkArgument(loginUser.getCompany().isPresent(), "登录用户无公司信息");
        List<RoleEntity> roles = getBean(RoleEntityAction.class).findByIds(loginUser.getCompany().get(), roleIds);
        checkStoreRole(loginUser);
        checkStoreEmpRoles(roles);
        getBean(EmployeeEntityAction.class).saveStoreEmployee(loginUser, store, loginName, userName, phoneNo, sex, roles);
    }

    /**
     * 验证当前登录用户是否有权限操作组织职员
     *
     * @param loginUser
     */
    private void checkOrgRole(LoginUserContext loginUser) {
        Preconditions.checkState(
                loginUser.getMaxPowerRole().isPresent() && (loginUser.getMaxPowerRole().get().isAdmin()
                        || loginUser.getMaxPowerRole().get().isManager() || loginUser.getMaxPowerRole().get().isBoss()),
                "登录用户无操作组织职员权限");
    }

    /**
     * 验证当前登录用户是否有权限操作门店职员
     *
     * @param loginUser
     */
    private void checkStoreRole(LoginUserContext loginUser) {
        Preconditions.checkState(
                loginUser.getMaxPowerRole().isPresent() && (!loginUser.getMaxPowerRole().get().isShoppingGuide()),
                "登录用户无操作门店职员权限");
    }

    private void checkOrgEmpRoles(Collection<RoleEntity> roles) {
        boolean isRigthRoles = true;
        for (RoleEntity role : roles) {
            if (!role.isManager())
                isRigthRoles = false;
        }
        Preconditions.checkState(isRigthRoles, "组织职员角色只能是【经理】");
    }

    private void checkStoreEmpRoles(Collection<RoleEntity> roles) {
        boolean isRigthRoles = true;
        for (RoleEntity role : roles) {
            if (!(role.isStoreManager() || role.isShoppingGuide()))
                isRigthRoles = false;
        }
        Preconditions.checkState(isRigthRoles, "门店职员角色只能是【店长】或【导购】");
    }

    /**
     * 编辑组织会员
     *
     * @param loginUser
     * @param empId
     * @param userName
     * @param phoneNo
     * @param sex
     * @param roleIds
     */
    public void editOrgEmployee(LoginUserContext loginUser, Integer orgId, Integer empId, String userName, String phoneNo,
                                int sex, Collection<Integer> roleIds) {
        Preconditions.checkNotNull(loginUser, "入参loginUser不能为空");
        Preconditions.checkNotNull(empId, "入参empId不能为空");
        Preconditions.checkNotNull(orgId, "入参orgId不能为空");
        checkOrgRole(loginUser);
        List<RoleEntity> roles = getBean(RoleEntityAction.class).findByIds(loginUser.getCompany().get(), roleIds);
        checkOrgEmpRoles(roles);
        getBean(EmployeeEntityAction.class).editEmployee(loginUser, empId, userName, phoneNo, sex, roles);
    }


    /**
     * 编辑门店会员
     *
     * @param loginUser
     * @param empId
     * @param userName
     * @param phoneNo
     * @param sex
     * @param roleIds
     */
    public void editStoreEmployee(LoginUserContext loginUser, Integer storeId, Integer empId, String userName, String phoneNo,
                                  int sex, Collection<Integer> roleIds) {
        Preconditions.checkNotNull(loginUser, "入参loginUser不能为空");
        Preconditions.checkNotNull(empId, "入参empId不能为空");
        checkStoreRole(loginUser);
        StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
        List<RoleEntity> roles = getBean(RoleEntityAction.class).findByIds(loginUser.getCompany().get(), roleIds);
        checkStoreEmpRoles(roles);
        getBean(EmployeeEntityAction.class).editEmployee(loginUser, empId, userName, phoneNo, sex, roles);

    }

}
