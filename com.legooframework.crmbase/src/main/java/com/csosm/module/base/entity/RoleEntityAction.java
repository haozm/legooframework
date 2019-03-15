package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.commons.entity.OrderAbledUtil;
import com.csosm.module.menu.entity.ResEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RoleEntityAction extends BaseEntityAction<RoleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RoleEntityAction.class);
    private final Comparator<RoleEntity> comparable = Comparator.comparingInt(RoleEntity::getPriority).reversed();

    public RoleEntityAction() {
        super("RoleEntity", "adapterCache");
    }

    public RoleSet loadRoleSetByUser(EmployeeEntity employee) {
        Preconditions.checkNotNull(employee, "入参职员非法，无法获取角色信息...");
        if (employee.getRoleIds().isPresent()) {
            Collection<Integer> roleIds = employee.getRoleIds().get();
            List<RoleEntity> roles = loadByCompany(employee.getCompanyId().or(-1));
            List<RoleEntity> roleSet = roles.stream().filter(x -> roleIds.contains(x.getId()))
                    .sorted(comparable).collect(Collectors.toList());
            RoleSet res = new RoleSet(employee, roleSet);
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadRoleSetByUser(%s) has role %s", employee.getId(), roleIds));
            return res;
        }
        return new RoleSet(employee, null);
    }

    public List<RoleEntity> loadEnlabedRole(OrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参OrganizationEntity company为空，无法获取角色信息...");
        Preconditions.checkState(company.isCompany(), "入参必须是公司....");
        Integer companyId = company.getId();
        List<RoleEntity> roles = loadByCompany(companyId);
        List<RoleEntity> subRoles = roles.stream()
                .filter(x -> x.isManager() || x.isStoreManager() || x.isShoppingGuide() || x.isBoss())
                .collect(Collectors.toList());
        return OrderAbledUtil.reverse(subRoles);
    }

    public void clearAuthors(Integer roleId, OrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参 OrganizationEntity company 不可以为空....");
        Integer companyId = company.getId();
        List<RoleEntity> roleEntities = loadByCompany(companyId);
        if (CollectionUtils.isEmpty(roleEntities)) return;
        Optional<RoleEntity> exits_role = roleEntities.stream().filter(x -> x.getId().equals(roleId)).findFirst();
        if (exits_role.isPresent()) {
            RoleEntity roleEntity = exits_role.get();
            Optional<RoleEntity> clone = roleEntity.clearResources();
            clone.ifPresent(x -> {
                Map<String, Object> params = Maps.newHashMap();
                params.put("id", x.getId());
                params.put("companyId", company.getId());
                getJdbc().update(getExecSql("clearResources", null), params);
                if (getCache().isPresent()) {
                    final String cache_key = String.format("%s_company_%s", getModel(), companyId);
                    getCache().get().invalidate(cache_key);
                }
            });
        }
        logProxy(SystemlogEntity.delete(this.getClass(), "clearAuthors", String.format("清空角色授权 %s", roleId),
                "角色操作"));
    }

    public void authorized(Integer roleId, List<ResEntity> resEntities, OrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参 OrganizationEntity company 不可以为空....");
        Integer companyId = company.getId();
        List<RoleEntity> roleEntities = loadByCompany(companyId);
        if (CollectionUtils.isEmpty(roleEntities)) return;
        Optional<RoleEntity> exits_role = roleEntities.stream().filter(x -> x.getId().equals(roleId)).findFirst();
        if (exits_role.isPresent()) {
            RoleEntity roleEntity = exits_role.get();
            List<String> ids = resEntities.stream().map(BaseEntity::getId).collect(Collectors.toList());
            Optional<RoleEntity> clone = roleEntity.authorized(ids);
            clone.ifPresent(x -> {
                getJdbc().update(getExecSql("authorizedResources", null), x.toMap());
                if (getCache().isPresent()) {
                    final String cache_key = String.format("%s_company_%s", getModel(), companyId);
                    getCache().get().invalidate(cache_key);
                }
            });
        }
        logProxy(SystemlogEntity.delete(this.getClass(), "authorized", String.format("%s 角色授权", roleId),
                "角色操作"));
    }

    public Optional<RoleEntity> findById(OrganizationEntity company, Integer id) {
        List<RoleEntity> roles = loadByCompany(company.getId());
        return roles.stream().filter(x -> x.getId().equals(id)).findFirst();
    }

    public List<RoleEntity> findByIds(OrganizationEntity company, Collection<Integer> ids){
    	List<RoleEntity> roles = loadByCompany(company.getId());
    	return roles.stream().filter(x -> ids.contains(x.getId())).collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    private List<RoleEntity> loadByCompany(Integer companyId) {
        final String cache_key = String.format("%s_company_%s", getModel(), companyId);
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().getIfPresent(cache_key);
            if (cache_val != null) return (List<RoleEntity>) cache_val;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", 1);
        List<RoleEntity> roles = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadByCompany", params), params, new RowMapperImpl());
        Preconditions.checkState(!CollectionUtils.isEmpty(roles), "公司 %s 对应的角色尚未初始化...", companyId);
        roles.sort(comparable);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByCompany(%s) return  %s", companyId, roles));
        if (getCache().isPresent()) {
            getCache().get().put(cache_key, roles);
        }
        return roles;
    }

    class RowMapperImpl implements RowMapper<RoleEntity> {
        @Override
        public RoleEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new RoleEntity(resultSet.getInt("id"), resultSet);
        }
    }

    @Override
    protected ResultSetExtractor<RoleEntity> getResultSetExtractor() {
        return null;
    }
}
