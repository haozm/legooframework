package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.BaseEntityAction;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeEntityAction extends BaseEntityAction<EmployeeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeEntityAction.class);

    public EmployeeEntityAction() {
        super("EmployeeEntity", "adapterCache");
    }

    public void authenticationRight(Integer employeeId, MemberEntity member, OrganizationEntity company) {
        Preconditions.checkNotNull(member);
        Preconditions.checkState(member.getStoreId().isPresent(), "非法的数据，会员缺失门店信息...");
        Optional<EmployeeEntity> employeeOpt = findByUserId(employeeId, company);
        Preconditions.checkState(employeeOpt.isPresent(), "Id = %d 对应的职员不存在...", employeeId);
        authenticationRight(employeeOpt.get(), member);
    }

    private String encode(CharSequence rawPassword) {
        byte[] encryptPassword = DigestUtils.md5(rawPassword.toString().getBytes());
        String base64Password = Base64.encodeBase64String(encryptPassword);
        BigInteger bi_text = new BigInteger(base64Password.getBytes(Charsets.UTF_8));
        BigInteger bi_r0 = new BigInteger("0933324145462219732314329");
        BigInteger bi_r1 = bi_r0.xor(bi_text);
        return bi_r1.toString(16);
    }

    @Deprecated
    @Override
    public Optional<EmployeeEntity> findById(Object id) {
        throw new RuntimeException("不再支持该方法");
    }

    /**
     * 新增组织职员
     *
     * @param org
     * @param loginName
     * @param userName
     * @param phoneNo
     * @param sex
     * @param roles
     */
    public void saveOrgEmployee(LoginUserContext loginUser, OrganizationEntity org,
                                String loginName, String userName,
                                String phoneNo, int sex, Collection<RoleEntity> roles) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "入参登录账号loginName不能为空");
        Preconditions.checkArgument(!CollectionUtils.isEmpty(roles), "职员角色roles不能为空");
        RoleSet roleSet = new RoleSet(roles);
        Preconditions.checkState(!(roleSet.hasShoppingGuideRole() || roleSet.hasStoreManagerRole()),
                "组织职员不能含有店长 或者 导购权限...");
        addEmployee(userName, EmployeeEntity.DEFAULT_PASSWORD, loginName, phoneNo, sex, null, null,
                roles, null, org, loginUser);
    }

    /**
     * 新增门店职员
     *
     * @param loginUser
     * @param store
     * @param loginName
     * @param userName
     * @param phoneNo
     * @param sex
     * @param roles
     */
    public void saveStoreEmployee(LoginUserContext loginUser, StoreEntity store, String loginName,
                                  String userName, String phoneNo, int sex, Collection<RoleEntity> roles) {
        Objects.requireNonNull(store, "入参门店store不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "入参登录账号loginName不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userName), "入参职员名称userName不能为空");
        Preconditions.checkArgument(!CollectionUtils.isEmpty(roles), "职员角色roles不能为空");
        RoleSet roleSet = new RoleSet(roles);
        Preconditions.checkState(roleSet.hasShoppingGuideRole() || roleSet.hasStoreManagerRole(),
                "门店职员只能是店长 或者 导购权限...");
        addEmployee(userName, EmployeeEntity.DEFAULT_PASSWORD, loginName, phoneNo, sex, null, null, roles,
                store, null, loginUser);
    }

    /**
     * 新增职员
     *
     * @param userName
     * @param passowrd
     * @param loginName
     * @param phoneNo
     * @param roles
     * @param loginUser
     */
    private void addEmployee(String userName, String passowrd, String loginName, String phoneNo,
                             int sex, String remarke, Date birthday, Collection<RoleEntity> roles, StoreEntity store,
                             OrganizationEntity org, LoginUserContext loginUser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(passowrd), "用户密码不可以为空值....");
        String pwd = encode(passowrd);
        EmployeeEntity storeEmp = null;
        if (store != null) {
            storeEmp = EmployeeEntity.addStoreEmployee(userName, pwd, loginName, phoneNo, sex, remarke,
                    birthday, store, roles, loginUser);
        } else if (org.isCompany()) {
            storeEmp = EmployeeEntity.addAdminEmployee(pwd, loginName, org, loginUser);
        } else {
            Preconditions.checkNotNull(org, "待添加人员组织的信息不可以为空...");
            storeEmp = EmployeeEntity.addOrgEmployee(userName, pwd, loginName, phoneNo, sex,
                    remarke, birthday, org, roles, loginUser);
        }
        Map<String, Object> params = storeEmp.toMap();
        Long exits = getJdbc().queryForObject(getExecSql("exitAccount", null), params, Long.class);
        Preconditions.checkState(exits != null && exits == 0, "已经存在同样的登陆账号，请重新尝试...");
        int result = getJdbc().update(getExecSql("insertEmp", null), params);
        Preconditions.checkState(result == 1, "新增职员%s 失败", storeEmp);
        clearCache(loginUser.getCompany().get().getId());
        logProxy(SystemlogEntity.create(this.getClass(), "addEmployee", String.format("新增职员[%s]", storeEmp), "人员管理"));
    }

    /**
     * 批量启用职员
     */
    public void enableEmployees(Collection<Integer> empIds, OrganizationEntity company) {
    	if(CollectionUtils.isEmpty(empIds)) return ;
        Optional<List<EmployeeEntity>> empes = loadAllByCompany(company.getId());
        if (!empes.isPresent()) return;
        List<EmployeeEntity> comEmps = empes.get().stream().filter(x -> empIds.contains(x.getId())).collect(Collectors.toList());
        List<EmployeeEntity> sub = Lists.newArrayList();
        comEmps.forEach(x -> x.enabled().ifPresent(sub::add));
        if (CollectionUtils.isEmpty(sub)) return;
        List<Integer> sub_empIds = sub.stream().map(BaseEntity::getId).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("empIds", sub_empIds);
        params.put("companyId", company.getId());
        getNamedParameterJdbcTemplate().update(getExecSql("update_enable_state", params), params);
        clearCache(company.getId());
        logProxy(SystemlogEntity.update(this.getClass(), "enableEmployees", String.format("启用职员[%s]", empIds), "人员管理"));
    }

    /**
     * 批量禁用职员
     */
    public void disableEmployees(Collection<Integer> empIds, OrganizationEntity company) {
    	if(CollectionUtils.isEmpty(empIds)) return ;
        Optional<List<EmployeeEntity>> empes = loadAllByCompany(company.getId());
        if (!empes.isPresent()) return;
        List<EmployeeEntity> comEmps = empes.get().stream().filter(x -> empIds.contains(x.getId())).collect(Collectors.toList());
        List<EmployeeEntity> sub = Lists.newArrayList();
        comEmps.forEach(x -> x.disabled().ifPresent(sub::add));
        if (CollectionUtils.isEmpty(sub)) return;
        List<Integer> sub_empIds = sub.stream().map(BaseEntity::getId).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("empIds", sub_empIds);
        params.put("companyId", company.getId());
        getNamedParameterJdbcTemplate().update(getExecSql("update_disable_state", params), params);
        clearCache(company.getId());
        logProxy(SystemlogEntity.update(this.getClass(), "disableEmployees", String.format("禁用职员[%s]", empIds), "人员管理"));
    }
    
    /**
     * 批量重置职员密码
     *
     * @param empIds
     */
    public void resetPassword(Collection<Integer> empIds, OrganizationEntity company) {
        Preconditions.checkArgument(!CollectionUtils.isEmpty(empIds));
        Optional<List<EmployeeEntity>> empes = loadEmployeesByOrgOrCom(company, empIds);
        if (!empes.isPresent()) return;
        String password = encode(EmployeeEntity.DEFAULT_PASSWORD);
        List<EmployeeEntity> sub = Lists.newArrayList();
        empes.get().forEach(x -> x.resetPassword(password).ifPresent(sub::add));
        if (CollectionUtils.isEmpty(sub)) return;
        List<Integer> sub_empIds = sub.stream().map(BaseEntity::getId).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("empIds", sub_empIds);
        params.put("companyId", company.getId());
        params.put("password", password);
        getJdbc().update(getExecSql("reset_password", params), params);
        clearCache(company.getId());
        logProxy(SystemlogEntity.update(this.getClass(), "resetPassword", String.format("重置密码职员[%s]", empIds), "人员管理"));
    }

    public void changePassword(Integer empId, String oldPwd, String newPwd, String confirmPwd, OrganizationEntity company) {
        Preconditions.checkNotNull(empId, "待修改密码的人员ID不可以为空值....");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(oldPwd), "职员原密码不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newPwd), "职员新密码不能为空");
        Preconditions.checkArgument(newPwd.equals(confirmPwd), "新密码与确认密码不一致");
        String oldEncodePwd = encode(oldPwd);
        String newEncodePwd = encode(newPwd);
        Optional<EmployeeEntity> employee = this.findByUserId(empId, company);
        Preconditions.checkState(employee.isPresent(), "ID=%s 对应的账号不存在...");
        Optional<EmployeeEntity> clone = employee.get().changePassword(oldEncodePwd, newEncodePwd);
        if (clone.isPresent()) {
            int result = getJdbc().update(getExecSql("update_password", null), clone.get().toMap());
            Preconditions.checkState(result == 1, "重新设置密码失败");
            if (getCache().isPresent()) getCache().get().invalidateAll();
            logProxy(SystemlogEntity.update(this.getClass(), "changePassword",
                    String.format("重新设置密码职员[%s]", clone.get().getId()), "人员管理"));
        }
    }

    /**
     * 修改职员信息
     *
     * @param userName
     * @param phoneNo
     * @param sex
     * @param roles
     */
    public void editEmployee(LoginUserContext loginUser, Integer empId, String userName, String phoneNo, int sex,
                             Collection<RoleEntity> roles) {
        Objects.requireNonNull(empId, "入参职员emp不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userName), "职员名称userName不能为空");
        Optional<EmployeeEntity> employees = findByUserId(empId, loginUser.getCompany().get());
        if (!employees.isPresent()) return;
        Optional<EmployeeEntity> clone = employees.get().modify(userName, phoneNo, sex, roles);
        if (!clone.isPresent()) return;
        clone.get().setModifyUser(loginUser);
        getJdbc().update(getExecSql("update_employee", null), clone.get().toMap());
        clearCache(loginUser.getCompany().get().getId());
        logProxy(SystemlogEntity.update(this.getClass(), "editEmployee", String.format("编辑职员[%s]", clone), "人员管理"));
    }

    /**
     * 启停控制
     *
     * @param empId
     * @param employeeState true 启用 false 停用
     */
    public void changeEmpState(Integer empId, boolean employeeState, OrganizationEntity company) {
        Optional<EmployeeEntity> employee = findByUserId(empId, company);
        if (!employee.isPresent()) return;
        java.util.Optional<EmployeeEntity> clone = employeeState ? employee.get().enabled() : employee.get().disabled();
        clone.ifPresent(x -> {
            getJdbc().update(getExecSql("changeEmpState", null), x.toMap());
            clearCache(company.getId());
        });
    }

    public void authenticationRight(EmployeeEntity employee, MemberEntity member) {
        Preconditions.checkNotNull(member);
        Preconditions.checkNotNull(employee);
        Preconditions.checkState(member.getStoreId().isPresent(), "非法的数据，会员缺失门店信息...");
        Preconditions.checkState(employee.getRoleIds().isPresent(), "Id = %d 对应的职员无角色信息...", employee.getId());
        Preconditions.checkState(employee.getStoreId().isPresent(), "当前职员无门店信息...");
        Preconditions.checkState(member.getStoreId().get().equals(employee.getStoreId().get()),
                "当前会员与导购属于不同门店，无法访问该会员...");
        Preconditions.checkState(employee.getRoleIds().get().contains(5) || employee.getRoleIds().get().contains(7),
                "当前职员无店长或者导购角色...");
        // 店长
        if (employee.getRoleIds().get().contains(5)) {
        } else if (employee.getRoleIds().get().contains(7)) {
            Map<String, Object> params = Maps.newHashMap();
            params.put("employeeId", employee.getId());
            params.put("memberId", member.getId());
            Long res = getJdbc().queryForObject(getExecSql("exitMemberWithDaogou", null), params, Long.class);
            Preconditions.checkState(res >= 1, "当前导购无权访问%s会员资料信息...", member.getName());
        }
    }

    public Optional<EmployeeEntity> findByLoginName(String accountNo, OrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参 company 所属公司不可以为空值...");
        Preconditions.checkState(company.isCompany(), "需指定人员所在的公司信息...");
        return findByLoginName(accountNo, company.getId());
    }

    private Optional<EmployeeEntity> findByLoginName(String accountNo, Integer companyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNo), "入参 登陆账户不可以为空值...");
        Optional<List<EmployeeEntity>> employees = loadAllByCompany(companyId);
        if (!employees.isPresent()) return Optional.absent();
        String accountNo_alia = String.format("%s_%s", accountNo, companyId);
        java.util.Optional<EmployeeEntity> employee = employees.get().stream()
                .filter(x -> x.isLoginName(accountNo) || x.isLoginName(accountNo_alia))
                .filter(EmployeeEntity::isEnabled).findFirst();
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByLoginName(%s,%s) return -> %s", accountNo, companyId, employee.orElse(null)));
        return Optional.fromNullable(employee.orElse(null));
    }

    /**
     * 分配导购 或者 解绑导购
     *
     * @param employeeId
     * @param members
     * @param store
     * @param isbuilding
     */
    public void buildMembers(Integer employeeId, Collection<MemberEntity> members, StoreEntity store,
                             boolean isbuilding, OrganizationEntity company) {
        Preconditions.checkNotNull(store);
        if (CollectionUtils.isEmpty(members))
            return;
        Optional<EmployeeEntity> employee = Optional.absent();
        if (isbuilding)
            Preconditions.checkNotNull(employeeId, "职员ID不可以为空值...");
        if (employeeId != null) {
            employee = findByUserId(employeeId, company);
            Preconditions.checkState(employee.isPresent(), "Id=%s 对应的职员不存在", employeeId);
            Preconditions.checkState(
                    employee.get().getStoreId().isPresent() && employee.get().getStoreId().get().equals(store.getId()));
            Preconditions.checkState(
                    employee.get().getRoleIds().isPresent() && (employee.get().getRoleIds().get().contains(5)
                            || employee.get().getRoleIds().get().contains(7)));
        }
        List<Map<String, Object>> insert_list = null;
        Map<String, Object> params = Maps.newHashMap();
        if (employee.isPresent()) {
            insert_list = Lists.newArrayList();
            for (MemberEntity mm : members) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("memberId", mm.getId());
                map.put("employeeId", employee.get().getId());
                insert_list.add(map);
            }
            params.put("items", insert_list);
            params.put("employee", true);
        } else {
            List<Integer> list = Lists.newArrayList();
            for (MemberEntity mm : members)
                list.add(mm.getId());
            params.put("items", list);
            params.put("employee", false);
        }
        getJdbc().update(getExecSql("clearRelateShip", params), params);
        if (isbuilding) {
            getJdbcTemplate().batchUpdate(getExecSql("insertRelateShip", null), insert_list, 200,
                    new ParameterizedPreparedStatementSetter<Map<String, Object>>() {
                        @Override
                        public void setValues(PreparedStatement ps, Map<String, Object> map) throws SQLException {
                            ps.setObject(1, map.get("memberId"));
                            ps.setObject(2, map.get("employeeId"));
                        }
                    });
        }
    }

    private void clearCache(Integer companyId) {
        if (getCache().isPresent()) {
            getCache().get().invalidate(String.format("%s_company_%s", getModel(), companyId));
        }
    }

    /**
     * 加载门店级别职员
     *
     * @param store
     * @param empIds
     * @return
     */
    public Optional<List<EmployeeEntity>> loadEmployeesByStore(StoreEntity store, Collection<Integer> empIds) {
        Preconditions.checkNotNull(store);
        Integer companyId = store.getCompanyId().or(-1);
        Optional<List<EmployeeEntity>> employees = loadAllByCompany(companyId);
        if (!employees.isPresent()) return Optional.absent();
        List<EmployeeEntity> com_emps = employees.get().stream().filter(x -> x.isStoreEmp(store))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(com_emps)) return Optional.absent();
        if (!CollectionUtils.isEmpty(empIds)) {
            com_emps = com_emps.stream().filter(x -> empIds.contains(x.getId())).collect(Collectors.toList());
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEmployeesByStore(%s,%s) size is %s", companyId, empIds,
                    CollectionUtils.isEmpty(com_emps) ? 0 : com_emps.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(com_emps) ? null : com_emps);
    }

    /**
     * 加载组织或者公司级别职员
     *
     * @param comOrOrg
     * @param empIds
     * @return
     */
    public Optional<List<EmployeeEntity>> loadEmployeesByOrgOrCom(OrganizationEntity comOrOrg, Collection<Integer> empIds) {
        Preconditions.checkNotNull(comOrOrg);
        Integer companyId = comOrOrg.isCompany() ? comOrOrg.getId() : comOrOrg.getMyCompanyId();
        Optional<List<EmployeeEntity>> employees = loadAllByCompany(companyId);
        if (!employees.isPresent()) return Optional.absent();
        List<EmployeeEntity> com_emps;
        if (comOrOrg.isCompany()) {
            com_emps = employees.get().stream().filter(EmployeeEntity::isCompanyEmp)
                    .collect(Collectors.toList());
        } else {
            com_emps = employees.get().stream().filter(x -> x.isOrgEmp(comOrOrg))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(com_emps)) return Optional.absent();
        if (!CollectionUtils.isEmpty(empIds)) {
            com_emps = com_emps.stream().filter(x -> empIds.contains(x.getId())).collect(Collectors.toList());
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadEmployeesByOrgOrCom(%s,%s) size is %s", companyId, empIds,
                    CollectionUtils.isEmpty(com_emps) ? 0 : com_emps.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(com_emps) ? null : com_emps);
    }

    public Optional<EmployeeEntity> findByUserId(Integer userId, OrganizationEntity company) {
        if (null == userId) return Optional.absent();
        Optional<List<EmployeeEntity>> employees = loadAllByCompany(company.getId());
        if (!employees.isPresent()) return Optional.absent();
        java.util.Optional<EmployeeEntity> employee = employees.get().stream().filter(x -> x.getId().equals(userId))
                .findFirst();
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByUserId(%s) size is %s", userId, employee.orElse(null)));
        return Optional.fromNullable(employee.orElse(null));
    }

    @SuppressWarnings("unchecked")
    private Optional<List<EmployeeEntity>> loadAllByCompany(Integer companyId) {
        Preconditions.checkNotNull(companyId, "公司ID不可以空值...");
        final String cache_key = String.format("%s_company_%s", getModel(), companyId);
        if (getCache().isPresent()) {
            Object emps = getCache().get().getIfPresent(cache_key);
            if (emps != null)
                return Optional.of((List<EmployeeEntity>) emps);
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        List<EmployeeEntity> entities = getJdbc().query(getExecSql("loadAllByCompany", null), params,
                new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompany(%s) size is %s", companyId,
                    CollectionUtils.isEmpty(entities) ? 0 : entities.size()));
        if (getCache().isPresent() && !CollectionUtils.isEmpty(entities)) {
            getCache().get().put(cache_key, entities);
        }
        return Optional.fromNullable(CollectionUtils.isEmpty(entities) ? null : entities);
    }
    

    @Override
    protected ResultSetExtractor<EmployeeEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<EmployeeEntity> {
        @Override
        public EmployeeEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<EmployeeEntity> {

        @Override
        public EmployeeEntity extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private EmployeeEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        Set<Integer> roleIds = Sets.newHashSet();
        String roles = resultSet.getString("roleIds");
        if (!Strings.isNullOrEmpty(roles)) {
            for (String $it : StringUtils.split(roles, ',')) {
                roleIds.add(Integer.valueOf($it));
            }
        }
        Object orgId = resultSet.getObject("organizationId");
        Integer organizationId = orgId == null ? null : (Integer) orgId == -1 ? null : (Integer) orgId;
        return new EmployeeEntity(resultSet.getInt("id"), resultSet.getInt("employeeState"),
                resultSet.getInt("employeeType"), resultSet.getString("userName"),
                resultSet.getInt("companyId"),
                resultSet.getObject("storeId") == null ? null : resultSet.getInt("storeId"),
                resultSet.getString("password"),
                organizationId,
                resultSet.getString("loginName"), resultSet.getString("phoneNo"), resultSet.getInt("sex"),
                resultSet.getString("remark"), resultSet.getDate("birthday"), resultSet.getInt("state"), roleIds);
    }
}
