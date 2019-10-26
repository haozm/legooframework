package com.legooframework.model.reactor.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;

public class ReactorLogEntityAction extends BaseEntityAction<ReactorLogEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ReactorLogEntityAction.class);

    public ReactorLogEntityAction() {
        super(null);
    }

    public void batchInsert(Collection<ReactorLogEntity> reactorLogs) {
        if (CollectionUtils.isEmpty(reactorLogs)) return;
        super.batchInsert("batchInsert", reactorLogs);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(Collection<ReactorLogEntity> ...) size is %s", reactorLogs.size()));
    }

    @Override
    protected RowMapper<ReactorLogEntity> getRowMapper() {
        return null;
    }
}
