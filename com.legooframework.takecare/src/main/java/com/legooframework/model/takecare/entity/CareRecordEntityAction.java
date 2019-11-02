package com.legooframework.model.takecare.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CareRecordEntityAction extends BaseEntityAction<CareRecordEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CareRecordEntityAction.class);

    public CareRecordEntityAction() {
        super(null);
    }

    public void batchInsert(Collection<CareRecordEntity> takeCareLogs) {
        if (CollectionUtils.isEmpty(takeCareLogs)) return;
        super.batchInsert("batchInsert", takeCareLogs);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(...) size is %d", takeCareLogs.size()));
    }

    public Optional<List<CareRecordEntity>> query4List() {
        return super.queryForEntities("query4list", null, getRowMapper());
    }

    @Override
    protected RowMapper<CareRecordEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<CareRecordEntity> {
        @Override
        public CareRecordEntity mapRow(ResultSet res, int i) throws SQLException {
            return new CareRecordEntity(res.getLong("id"), res);
        }
    }
}
