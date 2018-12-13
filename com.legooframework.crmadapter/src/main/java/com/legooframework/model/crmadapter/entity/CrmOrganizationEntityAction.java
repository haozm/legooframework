package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CrmOrganizationEntityAction extends BaseEntityAction<CrmOrganizationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmOrganizationEntityAction.class);

    public CrmOrganizationEntityAction() {
        super("CrmAdapterCache");
    }

    @SuppressWarnings("unchecked")
    public Optional<List<CrmOrganizationEntity>> loadAllCompany() {
        final String cache_key = String.format("%s_byall", getModelName());
        if (getCache().isPresent()) {
            List<CrmOrganizationEntity> entities = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(entities)) {
                if (logger.isDebugEnabled())
                    logger.debug("loadAllCompany() from cache by %s", cache_key);
                return Optional.of(entities);
            }
        }
        Optional<List<CrmOrganizationEntity>> coms = queryForEntities("loadAllCompany", null, getRowMapper());
        if (getCache().isPresent() && coms.isPresent())
            getCache().get().put(cache_key, coms.get());
        if (logger.isDebugEnabled())
            logger.debug(String.format("<%s> loadAllCompany() return %s", getModelName(), coms.map(List::size).orElse(0)));
        return coms;
    }

    public Optional<CrmOrganizationEntity> findCompanyById(Integer id) {
        Preconditions.checkNotNull(id, "入参 公司ID 不可以为空值...");
        Optional<List<CrmOrganizationEntity>> comps = loadAllCompany();
        return comps.isPresent() ? comps.get().stream().filter(x -> x.getId().equals(id)).findFirst() : Optional.empty();
    }

    @Override
    protected RowMapper<CrmOrganizationEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CrmOrganizationEntity> {
        @Override
        public CrmOrganizationEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new CrmOrganizationEntity(res.getInt("id"), res);
        }
    }
}
