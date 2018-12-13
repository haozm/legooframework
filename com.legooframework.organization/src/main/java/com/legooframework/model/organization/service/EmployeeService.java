package com.legooframework.model.organization.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LegooRole;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.organization.dto.EmployeeAgg;
import com.legooframework.model.organization.entity.*;
import com.legooframework.model.organization.event.OrgEventFactory;
import com.legooframework.model.organization.event.OrgModuleEvent;
import com.legooframework.model.security.entity.AccountEntity;
import com.legooframework.model.security.event.SecEventFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeService extends OrgService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final static String DEFAULT_PASSWORD = "123456";

    public List<EmployeeEntity> loadEmployeesByStoreId(Long storeId) {
        Objects.requireNonNull(storeId);
        StoreEntity store = getStoreEntityAction().loadById(storeId);
        Optional<List<EmployeeEntity>> employeesOpt = getEmployeeAction().loadAllByStore(store);
        return employeesOpt.orElseGet(Lists::newArrayList);
    }

    /**
     * 获取绑定账户的 职员实体
     *
     * @param account AccountEntity
     * @return 职员聚合
     */
    public Optional<EmployeeAgg> loadEmployeeAggByAccount(AccountEntity account) {
        Preconditions.checkNotNull(account, "入参 account 不可以为空.");
        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class).findByAccount(account);
        if (!employee.isPresent())
            return Optional.empty();
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(employee.get().getTenantId());
        Preconditions.checkState(company.isPresent(), "职员%s对应的公司不存在...", employee.get().getUserName());
        Optional<StorePermissionEntity> storePermission = getBean(StorePermissionAction.class)
                .findByEmployee(employee.get());
        if (storePermission.isPresent() && CollectionUtils.isNotEmpty(storePermission.get().getStoreIds())) {
            Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class).loadStoresByCompany(company.get(),
                    storePermission.get().getStoreIds());
            return Optional.of(new EmployeeAgg(employee.get(), company.get(), stores.orElse(null)));
        }
        return Optional.of(new EmployeeAgg(employee.get(), company.get(), null));
    }

    /**
     * 获取绑定账户的 职员实体
     *
     * @param employeeId Long
     * @return 职员聚合
     */
    public Optional<EmployeeAgg> loadEmployeeAggById(Long employeeId) {
        Preconditions.checkNotNull(employeeId, "入参 employeeId 不可以为空.");
        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class).findById(employeeId);
        if (!employee.isPresent())
            return Optional.empty();
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(employee.get().getTenantId());
        Preconditions.checkState(company.isPresent(), "职员%s对应的公司不存在...", employee.get().getUserName());
        Optional<StorePermissionEntity> storePermission = getBean(StorePermissionAction.class)
                .findByEmployee(employee.get());
        if (storePermission.isPresent() && CollectionUtils.isNotEmpty(storePermission.get().getStoreIds())) {
            Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class).loadStoresByCompany(company.get(),
                    storePermission.get().getStoreIds());
            return Optional.of(new EmployeeAgg(employee.get(), company.get(), stores.orElse(null)));
        }
        return Optional.of(new EmployeeAgg(employee.get(), company.get(), null));
    }

    private Optional<StoreEntity> loadByEmployee(EmployeeEntity employee) {
        if (employee.getStoreId().isPresent()) {
            Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(employee.getStoreId().get());
            Preconditions.checkState(store.isPresent(), "职员所在storeId=%s 对应的门店无法获取.", employee.getStoreId().get());
            return store.get().isEffective() ? store : Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * 设置职员为工作状态
     *
     * @param empId
     */
    public void activateEmployee(Long empId) {
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().inServiceAction(emp);
        if (logger.isInfoEnabled())
            logger.info("EmployeeService##activateEmployee 更新职员[{}]为工作状态成功", empId);
    }

    /**
     * 员工休假
     *
     * @param empId
     */
    public void furloughEmployee(Long empId) {
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        Preconditions.checkState(emp.isWorking(), "员工[%s] 不在职", empId);
        getEmployeeAction().inVacationAction(emp);
        if (logger.isInfoEnabled())
            logger.info("EmployeeService##furloughEmployee 更新职员[{}]为休假状态成功", empId);
    }

    /**
     * 员工离职
     *
     * @param empId
     */
    public void quitEmployee(String empId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(empId), "empId不能为空或null");
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().quitAction(emp);
        getEventBus().postEvent(OrgEventFactory.noticeQuitEmployeeEvent(emp));
        if (logger.isInfoEnabled())
            logger.info("EmployeeService##quitEmployee 更新职员[{}]为离职状态成功", empId);
    }

    /**
     * 修改职员信息
     *
     * @param empId
     * @param userName
     * @param userBirthday
     * @param userRemark
     * @param phoneNo
     */
    public void modifyEmployee(Long empId, String userName, Date userBirthday, String userRemark, String phoneNo,
                               Date employeeTime) {
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().editAction(emp, userName, userBirthday, userRemark, phoneNo, employeeTime);
        if (logger.isInfoEnabled())
            logger.info("EmployeeService##modifyEmployee 更新职员[{}]基本信息成功", empId);
    }

    /**
     * 新增公司职员
     *
     * @param userName
     * @param userSex
     * @param userBirthday
     * @param userRemark
     * @param workNo
     * @param phoneNo
     * @param lc
     * @return
     */
    public Long addCompanyEmployee(String workNo, String userName, KvDictDto userSex, Date userBirthday,
                                   String userRemark, String phoneNo, Date employeeTime, Long orgId, LoginContext lc,
                                   String... roleNos) {
        argumentsNotNullCheck(workNo, userName, userSex, userBirthday, userRemark, phoneNo, orgId);
        EmployeeEntity emp = getEmployeeAction().insert(workNo, userName, userSex, userBirthday, userRemark, phoneNo,
                employeeTime, null, orgId);
        AccountEntity account = createAccount(workNo, userName);
        getEmployeeAction().bindAccount(emp, account);
        if (roleNos.length != 0)
            authorizeRoles(account.getId(), roleNos);
        if (logger.isInfoEnabled())
            logger.info("EmployeeService##addStoreEmployee" + "新增公司职员[{}] 绑定登录账号[{}] 授予权限[{}] 成功", emp.getId(),
                    account.getId(), roleNos);
        postAddedEmployeeEvent(emp);
        return emp.getId();
    }

    private void postAddedEmployeeEvent(EmployeeEntity employee) {
        OrgModuleEvent event = OrgEventFactory.noticeAddedEmployeeEvent(employee);
        getEventBus().postEvent(event);
    }

    /**
     * 新增门店职员
     *
     * @param workNo
     * @param userName
     * @param userSex
     * @param userBirthday
     * @param userRemark
     * @param phoneNo
     * @param employeeTime
     * @param orgId
     * @param storeId
     * @param lc
     * @param roleNos
     * @return
     */
    public Long addStoreEmployee(String workNo, String userName, KvDictDto userSex, Date userBirthday,
                                 String userRemark, String phoneNo, Date employeeTime, Long orgId, Long storeId, LoginContext lc,
                                 String... roleNos) {
        argumentsNotNullCheck(workNo, userName, userSex, userBirthday, userRemark, phoneNo, orgId, storeId);
        EmployeeEntity emp = getEmployeeAction().insert(workNo, userName, userSex, userBirthday, userRemark, phoneNo,
                employeeTime, storeId, orgId);
        AccountEntity account = createAccount(workNo, userName);
        getEmployeeAction().bindAccount(emp, account);
        if (roleNos.length != 0)
            authorizeRoles(account.getId(), roleNos);
        if (logger.isInfoEnabled())
            logger.info("EmployeeService##addStoreEmployee" + "新增门店职员[{}] 绑定登录账号[{}] 授予权限[{}] 成功", emp.getId(),
                    account.getId(), roleNos);
        postAddedEmployeeEvent(emp);
        return emp.getId();
    }

    /**
     * 参数不为空检验
     *
     * @param args
     */
    private void argumentsNotNullCheck(Object... args) {
        Arrays.stream(args).forEach(Objects::requireNonNull);
    }

    /**
     * 移除职员
     *
     * @param empId
     */
    public void removeEmployee(Long empId) {
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().remove(emp);
    }

    /**
     * 编辑职员信息
     *
     * @param empId
     * @param userName
     * @param userBirthday
     * @param userRemark
     * @param phoneNo
     * @param employeeTime
     */
    public void editEmployee(Long empId, String userName, Date userBirthday, String userRemark, String phoneNo,
                             Date employeeTime, String[] roleNos) {
        argumentsNotNullCheck(empId, userName, userBirthday, phoneNo);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().editAction(emp, userName, userBirthday, userRemark, phoneNo, employeeTime);
        if (roleNos != null && roleNos.length != 0) {
            if (!emp.getAccountId().isPresent())
                return;
            authorizeRoles(emp.getAccountId().get(), roleNos);
        }
    }

    /**
     * 设置职员为工作状态
     *
     * @param empId
     */
    public void inServiceEmployee(Long empId) {
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().inServiceAction(emp);
    }

    /**
     * 设置职员为休假状态
     *
     * @param empId
     */
    public void inVacationEmployee(Long empId) {
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().inVacationAction(emp);
    }

    /**
     * 设置职员为离职状态
     *
     * @param empId
     */
    public void quitEmployee(Long empId) {
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        getEmployeeAction().quitAction(emp);
    }

    /**
     * 给组织分配职员
     *
     * @param storeId
     */
    public void assignToOrg(Long orgId, Long storeId, Long empId) {
        Objects.requireNonNull(orgId);
        Objects.requireNonNull(storeId);
        Objects.requireNonNull(empId);
        EmployeeEntity emp = getEmployeeAction().loadById(empId);
        StoreEntity store = getStoreEntityAction().loadById(storeId);
        getEmployeeAction().assginToStore(store, orgId, emp);
    }

    /**
     * 获取当前登录用户可用角色
     *
     * @return
     */
    public Optional<LegooRole> loadLoginUserMaxEnabledRole() {
        LegooRole maxRole = null;
        try {
            Optional<Object> opt = getEventBus()
                    .sendAndReceive(SecEventFactory.loadLoginUserEnabledRolesEvent(getLocalBundle()), Object.class);
            Preconditions.checkState(opt.isPresent(), "获取当前登录用户可用角色失败");
            List<LegooRole> roles = (List<LegooRole>) opt.get();
            if (roles.isEmpty())
                return Optional.empty();
            maxRole = CommonsUtils.getOrdering().max(roles);
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error("EmployeeService##loadEnabledRoles 获取当前登录用户可用角色失败.");
        }
        if (maxRole == null)
            return Optional.empty();
        return Optional.of(maxRole);
    }

    public boolean checkEditable(List<LegooRole> roles, String... roleNos) {
        Optional<LegooRole> loginMaxRoleOpt = loadLoginUserMaxEnabledRole();
        Optional<LegooRole> employeeMaxRoleOpt = getMaxRole(roles, roleNos);
        if (!loginMaxRoleOpt.isPresent())
            return false;
        if (employeeMaxRoleOpt.isPresent()) {
            LegooRole loginMaxRole = loginMaxRoleOpt.get();
            LegooRole employeeMaxRole = employeeMaxRoleOpt.get();
            if (loginMaxRole.getIndex() - employeeMaxRole.getIndex() >= 0)
                return true;
        }
        return false;
    }

    /**
     * 从事件中当前系统下所有的角色列表
     *
     * @return
     */
    public List<LegooRole> getAllRoles() {
        try {
            Optional<Object> opt = getEventBus().sendAndReceive(SecEventFactory.loadAllRolesEvent(getLocalBundle()),
                    Object.class);
            Preconditions.checkState(opt.isPresent(), "获取当前登录用户可用角色失败");
            if (opt.isPresent())
                return (List<LegooRole>) opt.get();
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error("EmployeeService##getAllRoles 获取当前登录用户可用角色失败.");
        }
        return Lists.newArrayList();
    }

    /**
     * 获取最大的角色
     *
     * @param roleNos
     * @return
     */
    public Optional<LegooRole> getMaxRole(List<LegooRole> roles, String... roleNos) {
        List<LegooRole> empRoles = roles.stream().filter(x -> Lists.newArrayList(roleNos).contains(x.getRoleNo()))
                .collect(Collectors.toList());
        if (empRoles.isEmpty()) return Optional.empty();
        LegooRole maxRole = CommonsUtils.getOrdering().max(empRoles);
        return Optional.of(maxRole);
    }

    /**
     * 给账号授权
     *
     * @param roleNos
     */
    private void authorizeRoles(Long accountId, String... roleNos) {
        try {
            getEventBus().sendAndReceive(SecEventFactory.authorizedRolesEvent(getLocalBundle(), accountId, roleNos),
                    Object.class);
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error("EmployeeService##authorizeRoles 为账号[{}] 授权[{}] 失败.", new Object[]{accountId, roleNos});
        }
    }

    /**
     * 创建登录账号
     *
     * @param workNo
     * @param userName
     * @return
     */
    private AccountEntity createAccount(String workNo, String userName) {
        Optional<AccountEntity> accountOpt = null;
        try {
            accountOpt = getEventBus().sendAndReceive(
                    SecEventFactory.createAccountEvent(getLocalBundle(), workNo, userName, DEFAULT_PASSWORD),
                    AccountEntity.class);
            Preconditions.checkState(accountOpt.isPresent(), "为职员创建账号失败");
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error("EmployeeService##createAccount 职员[{}]绑定账号发生异常!", workNo);
            throw new IllegalStateException("职员绑定账号失败");
        }
        return accountOpt.get();
    }

}
