package com.legooframework.model.regiscenter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class TenantNetConfigEntityAction extends BaseEntityAction<TenantNetConfigEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TenantNetConfigEntityAction.class);

    public TenantNetConfigEntityAction() {
        super("ForeverCache");
    }

    @Override
    public Optional<TenantNetConfigEntity> findById(Object id) {
        throw new UnsupportedOperationException("不支持该方法调用...");
    }

    public Optional<TenantNetConfigEntity> findByCompany(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "company 入参非法,不允许为null...");
        String cache_key = String.format("%s_com_%s", getModelName(), company.getId());
        if (getCache().isPresent()) {
            TenantNetConfigEntity exits = getCache().get().get(cache_key, TenantNetConfigEntity.class);
            if (exits != null) return Optional.of(exits);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        Optional<TenantNetConfigEntity> configEntity = queryForEntity("findByCompany", params, getRowMapper());
        configEntity.ifPresent(y -> getCache().ifPresent(x -> x.put(cache_key, y)));
        configEntity.ifPresent(x -> x.setCompanyName(company.getName()));
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByCompany(%s) res is %s", company.getId(), configEntity.orElse(null)));
        return configEntity;
    }

    @Override
    protected RowMapper<TenantNetConfigEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<TenantNetConfigEntity> {
        @Override
        public TenantNetConfigEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new TenantNetConfigEntity(res.getLong("id"), res);
        }
    }

}
