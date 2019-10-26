package com.legooframework.model.reactor.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RetailFactEntityAction extends BaseEntityAction<RetailFactEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RetailFactEntityAction.class);

    public RetailFactEntityAction() {
        super(null);
    }

    public Map<String, Object> count4RetailSmsJob(OrgEntity company) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.getId());
        Optional<List<Map<String, Object>>> count = super.queryForMapList("count4RetailSmsJob", params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("count4RetailSmsJob() return %s", count.orElse(null)));
        if (!count.isPresent()) {
            // total=4, maxId=0
            Map<String, Object> res_map = Maps.newHashMap();
            res_map.put("total", 0);
            res_map.put("maxId", 0);
            return params;
        }
        return count.get().get(0);
    }

    Optional<List<RetailFactEntity>> findByParams(Map<String, Object> params) {
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    @Override
    protected RowMapper<RetailFactEntity> getRowMapper() {
        return new RetailFactRowMapper();
    }

}
