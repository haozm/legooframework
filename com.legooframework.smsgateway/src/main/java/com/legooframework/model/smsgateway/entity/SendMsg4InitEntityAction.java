package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class SendMsg4InitEntityAction extends BaseEntityAction<SendMsg4InitEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendMsg4InitEntityAction.class);

    public SendMsg4InitEntityAction() {
        super(null);
    }

    /**
     * 批量保存入库
     *
     * @param batchSaveLogs OOXX
     */
    public void batchInsert(Collection<SendMsg4InitEntity> batchSaveLogs) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(SendMsg4InitEntity...) size is %s", batchSaveLogs.size()));
        super.batchInsert(BATCHINSERT_SQL, 1024, batchSaveLogs);
    }

    private final String BATCHINSERT_SQL = "INSERT INTO SMS_TRANSPORT_LOG (id, company_id, store_id, member_id, send_batchno,  " +
            "phone_no, sms_count, word_count, member_name, sms_context, tenant_id, creator, free_send, sms_channel, " +
            "businsess_type, send_status, job_id, sms_enabled, communication_channel, weixin_id, device_id) " +
            " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ? )";

    @Override
    protected RowMapper<SendMsg4InitEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SendMsg4InitEntity> {
        @Override
        public SendMsg4InitEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SendMsg4InitEntity(res.getString("id"), res);
        }
    }
}
