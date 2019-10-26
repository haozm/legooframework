package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.commons.entity.CommunicationChannel;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SendMsg4SendEntityAction extends BaseEntityAction<SendMsg4SendEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendMsg4InitEntityAction.class);

    public SendMsg4SendEntityAction() {
        super(null);
    }

    private SqlUpdate update4Sending;

    private static String UPDATE_SQL = "UPDATE SMS_TRANSPORT_LOG SET send_status = ? ,send_res_code = ? , remarks = ? , send_local_date = ? WHERE id = ?";

    /**
     * 更新发送结果
     *
     * @param sendLog 发送结果跟踪
     */
    public void updateSendResulset(final SendMsg4SendEntity sendLog) {
        Objects.requireNonNull(getJdbcTemplate()).update(UPDATE_SQL, ps -> {
            ps.setObject(1, sendLog.getSendStatus().getStatus());
            ps.setObject(2, sendLog.getSendResCode());
            ps.setObject(3, sendLog.getRemarks());
            ps.setObject(4, sendLog.getSendLocalDate());
            ps.setObject(5, sendLog.getId());
        });
    }

    public void batchUpdate(List<SendMsg4SendEntity> smsSendLogs) {
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(UPDATE_SQL, smsSendLogs, smsSendLogs.size(), (ps, sendLog) -> {
            ps.setObject(1, sendLog.getSendStatus().getStatus());
            ps.setObject(2, sendLog.getSendResCode());
            ps.setObject(3, sendLog.getRemarks());
            ps.setObject(4, sendLog.getSendLocalDate());
            ps.setObject(5, sendLog.getId());
        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("共计完成短信发送状态更新 %s", smsSendLogs.size()));
    }

    public Optional<List<SendMsg4SendEntity>> load4Sending(ChargeSummaryEntity chargeSummary, CommunicationChannel communicationChannel) {
        // 修改发送状态 冲初始化 修改未 发送中
        update4Sending.update(chargeSummary.getSmsBatchNo());
        Map<String, Object> params = Maps.newHashMap();
        params.put("sendBatchNo", chargeSummary.getSmsBatchNo());
        params.put("communicationChannel", communicationChannel.getChannel());
        params.put("sql", "load4Sending");
        Optional<List<SendMsg4SendEntity>> smsBatchSendLogs = super.queryForEntities("load4Sending", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("load4Sending(%s) sise is %s", communicationChannel, smsBatchSendLogs.map(List::size).orElse(0)));
        smsBatchSendLogs.ifPresent(x -> x.forEach(SendMsg4SendEntity::toSending));
        return smsBatchSendLogs;
    }

    @Override
    protected void initTemplateConfig() {
        super.initTemplateConfig();
        update4Sending = new SqlUpdate(Objects.requireNonNull(getJdbcTemplate()).getDataSource(),
                "UPDATE SMS_TRANSPORT_LOG SET send_status = 1 WHERE send_batchno = ? AND send_status = 0");
        update4Sending.declareParameter(new SqlParameter("sendBatchno", Types.VARCHAR));
        update4Sending.compile();
    }

    @Override
    protected RowMapper<SendMsg4SendEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<SendMsg4SendEntity> {
        @Override
        public SendMsg4SendEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SendMsg4SendEntity(res.getString("id"), res);
        }
    }
}
