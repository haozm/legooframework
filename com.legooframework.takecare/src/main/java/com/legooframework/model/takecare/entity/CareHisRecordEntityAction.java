package com.legooframework.model.takecare.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;

public class CareHisRecordEntityAction extends BaseEntityAction<CareHisRecordEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CareHisRecordEntityAction.class);

    public CareHisRecordEntityAction() {
        super(null);
    }
    
    public void batchInert(Collection<CareHisRecordEntity> hisCareRecords) {
        if (CollectionUtils.isEmpty(hisCareRecords)) return;
        super.batchInsert("batchInsert", hisCareRecords);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInert(...) size is %d", hisCareRecords.size()));
    }

    @Override
    protected RowMapper<CareHisRecordEntity> getRowMapper() {
        return null;
    }
}

