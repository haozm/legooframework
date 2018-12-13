package com.legooframework.model.organization.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class StorePermissionAction extends BaseEntityAction<StorePermissionEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StorePermissionAction.class);

    public StorePermissionAction() {
        super("OrganizationCache");
    }

    @Override
    public Optional<StorePermissionEntity> findById(Object id) {
        throw new UnsupportedOperationException("该方法不再被支持....");
    }

    /**
     * @return 通过登陆用户获取对应的数据权限设定
     */
    public Optional<StorePermissionEntity> findByLoginUser(LoginContext loginUser) {
        Preconditions.checkNotNull(loginUser, "入参 LoginContext loginUser 不可以为空...");
        LoginContextHolder.setCtx(loginUser);
        return findByEmployeeId(loginUser.getLoginId());
    }

    /**
     * @return 通过登陆用户获取对应的数据权限设定
     */
    public Optional<StorePermissionEntity> findByEmployee(EmployeeEntity employee) {
        Preconditions.checkNotNull(employee, "入参 EmployeeEntity employee 不可以为空...");
        LoginContext user = LoginContextHolder.get();
        if (user.isAnonymous()) {
            LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx(employee.getTenantId()));
        }
        return findByEmployeeId(employee.getId());
    }

    private Optional<StorePermissionEntity> findByEmployeeId(Long employeeId) {
        Preconditions.checkNotNull(employeeId, "入参 Long employeeId 不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        final String cache_key = String.format("%s_employeeId_%s", getModelName(), employeeId);
        if (getCache().isPresent()) {
            Optional<StorePermissionEntity> cache_val = getCache().map(c -> c.get(cache_key, StorePermissionEntity.class));
            if (cache_val.isPresent()) {
                if (logger.isTraceEnabled())
                    logger.trace(String.format("Hit StorePermissionEntity cache_key = %s From Cache", cache_key));
                return cache_val;
            }
        }
        params.put("employeeId", employeeId);
        Optional<StorePermissionEntity> entity = queryForEntity(getStatementFactory(), getModelName(), "findByEmployeeId",
                params, getRowMapper());
        entity.ifPresent(x -> getCache().ifPresent(c -> c.put(cache_key, x)));
        return entity;
    }

    public boolean changePermission(EmployeeEntity employee, Collection<StoreEntity> stores,
                                    Collection<StoreTreeEntity> orgs) {
        Preconditions.checkNotNull(employee, "入参 EmployeeEntity employee 不可以为空值...");
        Optional<StorePermissionEntity> exits = findByEmployee(employee);
        Preconditions.checkState(exits.isPresent(), "职员%s对应的数据权限不存在，无法执行修改操作...");

        if (CollectionUtils.isEmpty(stores) && CollectionUtils.isEmpty(orgs)) {
            int res = super.updateAction(exits.get(), "deleteByEmployeeId");
            if (res == 1) {
                final String cache_key = String.format("%s_employeeId_%s", getModelName(), employee.getId());
                getCache().ifPresent(c -> c.evict(cache_key));
            }
            return true;
        }

        Optional<StorePermissionEntity> clone = exits.get().changePermission(stores, orgs);
        if (!clone.isPresent()) return false;
        int res = super.updateAction(clone.get(), "changePermission");

        if (res == 1) {
            final String cache_key = String.format("%s_employeeId_%s", getModelName(), employee.getId());
            getCache().ifPresent(c -> c.evict(cache_key));
        }
        return 1 == res;
    }

    /**
     * @return 新增职员对应的数据权限
     */
    public Long insert(LoginContext loginUser, EmployeeEntity employee,
                       Collection<StoreTreeEntity> orgs, Collection<StoreEntity> stores) {
        Preconditions.checkNotNull(loginUser, "入参 LoginContext loginUser 不可以为空...");
        Preconditions.checkNotNull(employee, "入参 EmployeeEntity employee 不可以为空...");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(orgs) || CollectionUtils.isNotEmpty(stores),
                "必须指定职员对应的数据权限数据...orgs OR stores");
        LoginContextHolder.setCtx(loginUser);
        idGenerator++;
        long id = idGenerator;
        StorePermissionEntity entity = new StorePermissionEntity(id, loginUser,
                employee, orgs, stores);
        Optional<StorePermissionEntity> exits = findByEmployee(employee);
        Preconditions.checkState(!exits.isPresent(), "职员%s对应的数据权限记录已经存在...", employee.getUserName());
        int res = update(getStatementFactory(), getModelName(), "insert", entity);
        Preconditions.checkState(1 == res, "新增职员%s对应的数据权限持久化失败...", employee.getUserName());
        return entity.getId();
    }

    @Override
    protected RowMapper<StorePermissionEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<StorePermissionEntity> {
        @Override
        public StorePermissionEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new StorePermissionEntity(res.getLong("id"), res);
        }
    }

    private long idGenerator;

    public void init() {
        long max_id = queryForLong("SELECT MAX(id) FROM org_store_permission ", 1L);
        this.idGenerator = max_id + 1;
    }
}
