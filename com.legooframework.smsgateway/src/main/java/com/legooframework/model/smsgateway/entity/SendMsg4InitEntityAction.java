package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SendMsg4InitEntityAction extends BaseEntityAction<SendMsg4InitEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendMsg4InitEntityAction.class);

    public SendMsg4InitEntityAction() {
        super(null);
    }

    public void batchTradeChannelInsert(Collection<SMSEntity> smses, StoEntity store, String batchNo, boolean free,
                                        BusinessType businessType) {
        if (CollectionUtils.isEmpty(smses)) return;
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(smses=%d,batchNo=%s,storeId=%d,businessType=%s)",
                    smses.size(), batchNo, store.getId(), businessType));
        List<SendMsg4InitEntity> instances = Lists.newArrayListWithCapacity(smses.size());
        smses.forEach(sms -> instances.add(SendMsg4InitEntity.createInstance(store, sms, batchNo, SMSChannel.TradeChannel,
                free, businessType)));
        super.batchInsert(BATCHINSERT_SQL, 1024, instances);
        insertBatchInfo(store, batchNo);
    }

    public void batchMarketChannelInsert(Collection<SMSEntity> smses, StoEntity store, String batchNo, boolean free,
                                         BusinessType businessType) {
        if (CollectionUtils.isEmpty(smses)) return;
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(smses=%d,batchNo=%s,storeId=%d,businessType=%s)",
                    smses.size(), batchNo, store.getId(), businessType));
        List<SendMsg4InitEntity> instances = Lists.newArrayListWithCapacity(smses.size());
        smses.forEach(sms -> instances.add(SendMsg4InitEntity.createInstance(store, sms, batchNo, SMSChannel.MarketChannel,
                free, businessType)));
        super.batchInsert(BATCHINSERT_SQL, 1024, instances);
        insertBatchInfo(store, batchNo);
    }

    private void insertBatchInfo(StoEntity store, String batchNo) {
        String insert_sql = "INSERT INTO SMS_TRANSPORT_BATCH  (company_id, store_id, send_batchno, is_billing, delete_flag, tenant_id) VALUES( ?, ?, ?,  0,  0, ?)";
        Objects.requireNonNull(getJdbcTemplate()).update(insert_sql, ps -> {
            ps.setObject(1, store.getCompanyId());
            ps.setObject(2, store.getId());
            ps.setObject(3, batchNo);
            ps.setObject(4, store.getCompanyId());
        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("insertBatchInfo(store=%d,batchNo=%s) is finshed", store.getId(), batchNo));
    }

    private final String BATCHINSERT_SQL = "INSERT INTO SMS_TRANSPORT_LOG (id, company_id, store_id, member_id, send_batchno, " +
            "phone_no, sms_count, word_count, member_name, sms_context, tenant_id, creator, free_send, sms_channel, " +
            "businsess_type, send_status, job_id, sms_enabled, communication_channel, weixin_id, device_id) " +
            " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ? )";

    private final String INSERT_SQL = "";

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
