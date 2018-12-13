package com.legooframework.model.organization.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.security.entity.AccountEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EmployeeEntityAction extends BaseEntityAction<EmployeeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeEntityAction.class);

    public EmployeeEntityAction() {
        super("OrganizationCache");
    }

    /**
     * 新增职员
     *
     * @param workNo
     * @param userName
     * @param userSex
     * @param userBirthday
     * @param userRemark
     * @param phoneNo
     * @param employeeTime
     * @param storeId
     * @param orgId
     * @return
     */
    public EmployeeEntity insert(String workNo, String userName, KvDictDto userSex, Date userBirthday,
                                 String userRemark, String phoneNo, Date employeeTime, Long storeId, Long orgId) {
        checkWorkNoAndPhoneNo(workNo, phoneNo);
        idGenerator++;
        Long id = idGenerator;
        LoginContext lc = LoginContextHolder.get();
        EmployeeEntity entity = new EmployeeEntity(id, workNo, userName, userSex, userBirthday, userRemark, phoneNo,
                employeeTime, orgId, storeId, lc);
        int res = updateAction(entity, "insert");
        Preconditions.checkState(1 == res, "新增职员%s写入数据库失败.", userName);
        if (entity.getStoreId().isPresent())
            insertStoreMapEmployee(storeId, id);
        return entity;
    }

    /**
     * 新增门店与职员对应关系
     *
     * @param storeId
     * @param empId
     */
    private void insertStoreMapEmployee(Long storeId, Long empId) {
        Map<String, Object> params = LoginContextHolder.get().toParams();
        params.put("storeId", storeId);
        params.put("employeeId", empId);
        String sql = getStatementFactory().getExecSql(getModelName(), "insert_store_map_employee", null);
        int res = getNamedParameterJdbcTemplate().update(sql, params);
        Preconditions.checkState(res == 1, "新增职员[%s]MAP门店[%s]失败", empId, storeId);
    }

    /**
     * 检验工作号及电话号码是否存在
     *
     * @param workNo
     * @param phoneNo
     */
    private void checkWorkNoAndPhoneNo(String workNo, String phoneNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(workNo), "入参workNo不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(phoneNo), "入参phoneNo不能为空");
        Map<String, Object> params = Maps.newHashMap();
        params.put("workNo", workNo);
        params.put("phoneNo", phoneNo);
        Optional<List<EmployeeEntity>> exits = queryForEntities(getStatementFactory(), getModelName(), "exits", params,
                getRowMapper());
        Preconditions.checkState(!exits.isPresent(), "存在工号 %s 或者手机号码 %s 重复的职员信息，请检查数据.", workNo, phoneNo);
    }

    public Optional<List<EmployeeEntity>> loadAllByStore(StoreEntity store) {
        Preconditions.checkNotNull(store, "入参 Store 不可以为空.");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        return queryForEntities("loadAllByStore", params, getRowMapper());
    }

    /**
     * 员工被聘用
     *
     * @param empId
     * @return
     */
    public EmployeeEntity inServiceAction(EmployeeEntity emp) {
        Objects.requireNonNull(emp);
        if (emp.isInService())
            return emp;
        EmployeeEntity clone = emp.doInService();
        int res = updateAction(clone, "update_workstatus");
        Preconditions.checkState(res == 1, "更新职员状态失败");
        return clone;
    }

    /**
     * 员工请假
     *
     * @param empId
     * @return
     */
    public EmployeeEntity inVacationAction(EmployeeEntity emp) {
        Objects.requireNonNull(emp);
        if (emp.isInVacation())
            return emp;
        EmployeeEntity clone = emp.doInVacation();
        int res = updateAction(clone, "update_workstatus");
        Preconditions.checkState(res == 1, "更新职员状态失败");
        return clone;
    }

    /**
     * 员工离职
     *
     * @param empId
     * @return
     */
    public EmployeeEntity quitAction(EmployeeEntity emp) {
        Objects.requireNonNull(emp);
        if (emp.isQuited())
            return emp;
        EmployeeEntity clone = emp.doQuit();
        int res = updateAction(clone, "update_workstatus");
        Preconditions.checkState(res == 1, "更新职员状态失败");
        return clone;
    }

    /**
     * 编辑员工信息
     *
     * @return
     */
    public EmployeeEntity editAction(EmployeeEntity emp, String userName, Date userBirthday, String userRemark,
                                     String phoneNo, Date employeeTime) {
        Objects.requireNonNull(emp);
        EmployeeEntity clone = emp.modify(userName, userBirthday, userRemark, phoneNo, employeeTime);
        if (emp.equals(clone))
            return clone;
        int res = updateAction(clone, "update");
        Preconditions.checkState(res == 1, "更新职员基本信息失败");
        return clone;
    }

    @Override
    protected void cacheEntity(EmployeeEntity entity) {
        if (null == entity)
            return;
        super.cacheEntity(entity);
        getCache().ifPresent(c -> entity.getAccountId()
                .ifPresent(x -> c.put(String.format("%s_account_%s", getModelName(), x), entity)));
    }

    @Override
    protected void evictEntity(EmployeeEntity entity) {
        if (null == entity)
            return;
        super.evictEntity(entity);
        getCache().ifPresent(
                c -> entity.getAccountId().ifPresent(x -> c.evict(String.format("%s_account_%s", getModelName(), x))));
    }

    /**
     * 绑定账号
     *
     * @param employee
     * @param account
     * @return
     */
    public boolean bindAccount(EmployeeEntity employee, AccountEntity account) {
        Preconditions.checkNotNull(employee, "入参 employee 不可以为空.");
        Preconditions.checkNotNull(account, "入参 account 不可以为空.");
        EmployeeEntity clone = employee.bindAccount(account);
        int res = updateAction(clone, "bildAccount");
        Preconditions.checkState(1 == res, "持久化绑定账户信息失败.");
        evictEntity(clone);
        return true;
    }

    /**
     * 删除职员
     *
     * @param employee
     */
    public void remove(EmployeeEntity employee) {
        Objects.requireNonNull(employee);
        updateAction(employee, "delete_employee");
        updateAction(employee, "delete_store_employee");
    }

    /**
     * 分配给门店
     *
     * @param store
     * @param emp
     * @return
     */
    public EmployeeEntity assginToStore(StoreEntity store, Long orgId, EmployeeEntity emp) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(emp);
        updateAction(emp, "delete_store_employee");
        emp.setOrgId(orgId);
        updateAction(emp, "update_employee_orgId");
        insertStoreMapEmployee(store.getId(), emp.getId());
        emp.setStoreId(store.getId());
        return emp;
    }

    /**
     * @param account
     * @return
     */
    public Optional<EmployeeEntity> findByAccount(AccountEntity account) {
        Preconditions.checkNotNull(account, "登陆账号不可以为空");
        final String cache_key = String.format("%s_account_%s", getModelName(), account.getId());
        if (getCache().isPresent()) {
            Optional<EmployeeEntity> cache_val = getCache().map(c -> c.get(cache_key, EmployeeEntity.class));
            if (cache_val.isPresent()) {
                if (logger.isTraceEnabled())
                    logger.trace(String.format("Hit cache_key = %s From Cache", cache_key));
                return cache_val;
            }
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("accountId", account.getId());
        params.put("tenantId", account.getTenantId());

        Optional<EmployeeEntity> exits = queryForEntity("findByAccount", params, getRowMapper());
        exits.ifPresent(x -> getCache().ifPresent(c -> c.put(cache_key, x)));
        return exits;
    }

    @Override
    protected RowMapper<EmployeeEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<EmployeeEntity> {
        @Override
        public EmployeeEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new EmployeeEntity(res.getLong("employeeId"), res);
        }
    }

    private long idGenerator;

    public void init() {
        long max_id = queryForLong("SELECT MAX(id) FROM org_employee_info ", 100L);
        this.idGenerator = max_id + 1;
    }
}
