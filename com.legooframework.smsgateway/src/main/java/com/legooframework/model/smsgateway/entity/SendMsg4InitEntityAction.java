package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.*;

public class SendMsg4InitEntityAction extends BaseEntityAction<SendMsg4InitEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendMsg4InitEntityAction.class);

    public SendMsg4InitEntityAction() {
        super(null);
    }

    public Optional<List<String>> loadNeedSyncStateSmsIds() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "loadNeedSyncStateSmsIds");
        String query_sql = getStatementFactory().getExecSql(getModelName(), "loadNeedSyncStateSmsIds", params);
        return super.queryForList(query_sql, params);
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

    public void updateSendState(SendMsg4SendEntity sendEntity) {
        String update_sql = "UPDATE SMS_TRANSPORT_LOG SET send_status= ?,send_res_code=?, send_local_date=?, remarks=? WHERE id = ?";
        Objects.requireNonNull(getJdbcTemplate()).update(update_sql, ps -> {
            ps.setObject(1, sendEntity.getSendStatus().getStatus());
            ps.setObject(2, sendEntity.getSendResCode());
            ps.setObject(3, sendEntity.getSendLocalDate());
            ps.setObject(4, sendEntity.getRemarks());
            ps.setObject(5, sendEntity.getId());
        });
    }

    public void updateFinalState(Collection<SendMsg4FinalEntity> finalStates) {
        if (CollectionUtils.isEmpty(finalStates)) return;
        String update_sql = "UPDATE SMS_TRANSPORT_LOG SET send_status = ?, final_state= ?, final_state_date = ?, final_state_desc= ? WHERE id = ?";
        super.batchInsert(update_sql, 512, finalStates);
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateFinalState() update size is %s", finalStates.size()));
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
