package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class OrgEntityAction extends BaseEntityAction<OrgEntity> {

    public OrgEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public OrgEntity loadComById(Integer companyId) {
        List<OrgEntity> orgs = loadAllOrgsByCompanyId(companyId);
        Optional<OrgEntity> com = orgs.stream().filter(org -> Objects.equals(org.getId(), companyId)).findFirst();
        Preconditions.checkState(com.isPresent());
        return com.get();
    }

    public Optional<OrgEntity> findById(Integer companyId, Integer id) {
        List<OrgEntity> orgs = loadAllOrgsByCompanyId(companyId);
        return orgs.stream().filter(org -> Objects.equals(org.getId(), id)).findFirst();
    }

    public OrgEntity loadOrgByStore(StoEntity sto) {
        List<OrgEntity> orgs = loadAllOrgsByCompanyId(sto.getCompanyId());
        Optional<OrgEntity> exits = orgs.stream().filter(org -> Objects.equals(org.getId(), sto.getOrgId())).findFirst();
        Preconditions.checkState(exits.isPresent(), "门店 =%s 对应的机构不存在...", sto);
        return exits.get();
    }

    List<OrgEntity> loadAllOrgsByCompanyId(Integer companyId) {
        Preconditions.checkArgument(companyId != null && companyId > 0, "非法的入参 companyId = %s", companyId);
        final String cache_key = String.format("ORG_ALL_%s", companyId);
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<OrgEntity> cache_list = (List<OrgEntity>) getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(cache_list)) return cache_list;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("sql", "loadAllByCompanyId");
        Optional<List<OrgEntity>> all_orgs = super.queryForEntities("query4list", params, getRowMapper());
        Preconditions.checkState(all_orgs.isPresent(), "companyId = %s 无组织公司信息", companyId);
        getCache().ifPresent(c -> c.put(cache_key, all_orgs.get()));
        return all_orgs.get();
    }

    @Override
    protected RowMapper<OrgEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<OrgEntity> {
        @Override
        public OrgEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new OrgEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
