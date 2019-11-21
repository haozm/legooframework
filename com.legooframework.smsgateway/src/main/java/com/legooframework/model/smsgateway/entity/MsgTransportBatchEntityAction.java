package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MsgTransportBatchEntityAction extends BaseEntityAction<MsgTransportBatchEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MsgTransportBatchEntityAction.class);

    public MsgTransportBatchEntityAction() {
        super(null);
    }

    public void insert(StoEntity store, String batchNo, Collection<SendMsg4InitEntity> messages) {
        MsgTransportBatchEntity instance = new MsgTransportBatchEntity(store, batchNo, messages);
        super.batchInsert("insert", Lists.newArrayList(instance));
        if (logger.isDebugEnabled())
            logger.debug(String.format("Save MsgTransportBatchEntity (%s) finish", instance.toString()));
    }

    public void finishBilling(MsgTransportBatchEntity transportBatch) {
        String update_sql = "UPDATE SMS_TRANSPORT_BATCH SET is_billing =  1 WHERE send_batchno = ?";
        Objects.requireNonNull(getJdbcTemplate()).update(update_sql, transportBatch.getBatchNo());
        if (logger.isDebugEnabled())
            logger.debug(String.format("finishBilling(%s)", transportBatch.getBatchNo()));
    }

    public MsgTransportBatchEntity loadByBatchNo(String batchNo) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("bathNo", batchNo);
        params.put("sql", "loadByBatchNo");
        Optional<MsgTransportBatchEntity> optional = super.queryForEntity("query4list", params, getRowMapper());
        Preconditions.checkState(optional.isPresent(), "batchNo=%d 对应的实体不存在...", batchNo);
        return optional.get();
    }

    @Override
    protected RowMapper<MsgTransportBatchEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<MsgTransportBatchEntity> {
        @Override
        public MsgTransportBatchEntity mapRow(ResultSet res, int i) throws SQLException {
            return new MsgTransportBatchEntity(res.getLong("id"), res);
        }
    }
}
