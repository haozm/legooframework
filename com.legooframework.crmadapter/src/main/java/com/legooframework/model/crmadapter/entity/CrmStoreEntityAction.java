package com.legooframework.model.crmadapter.entity;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CrmStoreEntityAction extends BaseEntityAction<CrmStoreEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CrmStoreEntityAction.class);

    public CrmStoreEntityAction() {
        super("CrmAdapterCache");
    }

    /**
     * 加载指定公司的所有门店
     *
     * @param company
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<List<CrmStoreEntity>> loadAllByCompany(CrmOrganizationEntity company) {
        Preconditions.checkNotNull(company, "公司不可以为空...");
        final String cache_key = String.format("%s_bycompany_%s", getModelName(), company.getId());
        if (getCache().isPresent()) {
            List<CrmStoreEntity> entities = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(entities)) {
                if (logger.isDebugEnabled())
                    logger.debug("loadAllByCompany(store::%s) from cache by %s", company.getName(), cache_key);
                return Optional.of(entities);
            }
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        Optional<List<CrmStoreEntity>> stores = queryForEntities("loadAllByCompany", params, getRowMapper());
        if (getCache().isPresent() && stores.isPresent())
            getCache().get().put(cache_key, stores.get());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompany(company:%s) has store size is %s .", company.getId(),
                    stores.map(List::size).orElse(0)));
        return stores;
    }

    /**
     * 获取全部门店信息 不区分公司
     *
     * @return CrmStoreEntiies
     */
    public Optional<List<CrmStoreEntity>> loadAll() {
        Optional<List<CrmStoreEntity>> stores = queryForEntities("loadAll", null, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAll() stores size is %s .", stores.map(List::size).orElse(0)));
        return stores;
    }

    /**
     * @param company
     * @param storeId
     * @return
     */
    public Optional<CrmStoreEntity> findById(CrmOrganizationEntity company, Integer storeId) {
        Optional<List<CrmStoreEntity>> stores = loadAllByCompany(company);
        return stores.isPresent() ? stores.get().stream().filter(x -> x.getId().equals(storeId)).findFirst() :
                Optional.empty();
    }

    @Override
    protected RowMapper<CrmStoreEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CrmStoreEntity> {
        @Override
        public CrmStoreEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new CrmStoreEntity(resultSet.getInt("id"), resultSet);
        }
    }

}
