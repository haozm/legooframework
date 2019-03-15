package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SMSTransportLogEntityAction extends BaseEntityAction<SMSTransportLogEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSTransportLogEntityAction.class);

    public SMSTransportLogEntityAction() {
        super(null);
    }

    public void batchAdd4Init(Collection<SMSTransportLogEntity> smsTransportLogs) {
        super.batchInsert("batchInsert", smsTransportLogs);
    }

    /**
     * 获取已经存在的 短信ID
     *
     * @param batchNo     批次号
     * @param smsEntities 短信
     * @return ID集合
     */
    public Optional<List<String>> loadExitsTransportSMS(String batchNo, List<SMSEntity> smsEntities) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(smsEntities));
        Map<String, Object> params = Maps.newHashMap();
        params.put("batchNo", batchNo);
        List<String> smsIds = Lists.transform(smsEntities, SMSEntity::getSmsId);
        params.put("smsIds", smsIds);
        return super.queryForList("loadExitsTransportSMS", params);
    }

    public synchronized void updateLog4Storage(ChargeSummaryEntity billingSummary) {
        Preconditions.checkNotNull(billingSummary);
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsBatchNo", billingSummary.getSmsBatchNo());
        int size = super.updateAction("update4Storage", params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateLog4Storage(%s) size is %s", billingSummary.getSmsBatchNo(), size));
    }

    public synchronized Optional<List<SMSTransportLogEntity>> loadSms4Sending(ChargeSummaryEntity chargeSummary) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsBatchNo", chargeSummary.getSmsBatchNo());
        Optional<List<SMSTransportLogEntity>> transportLogs = super.queryForEntities("load4Sending", params, getRowMapper());
        transportLogs.ifPresent(logs -> {
            List<String> ids = logs.stream().map(BaseEntity::getId).collect(Collectors.toList());
            Map<String, Object> _params = Maps.newHashMap();
            _params.put("ids", ids);
            super.updateAction("update4Sending", _params);
            logs.forEach(SMSTransportLogEntity::set4Sending);
        });

        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSms4Sending() sise is %s", transportLogs.map(List::size).orElse(0)));
        return transportLogs;
    }


    public void updateErrorInstance(final SMSTransportLogEntity transportLog) {
        if (transportLog.isError()) {
            String sql = "UPDATE SMS_TRANSPORT_LOG SET send_status = ? ,res_code = ? , remarks = ? WHERE id = ?";
            Objects.requireNonNull(getJdbcTemplate()).update(sql, ps -> {
                ps.setObject(1, transportLog.getSendStatus().getStatus());
                ps.setObject(2, transportLog.getResCode());
                ps.setObject(3, transportLog.getRemarks());
                ps.setObject(4, transportLog.getId());
            });
        }
    }

    @Override
    protected RowMapper<SMSTransportLogEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<SMSTransportLogEntity> {
        @Override
        public SMSTransportLogEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSTransportLogEntity(res.getString("id"), res);
        }
    }
}
