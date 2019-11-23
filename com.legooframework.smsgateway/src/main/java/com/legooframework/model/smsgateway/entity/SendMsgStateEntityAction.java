package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SendMsgStateEntityAction extends BaseEntityAction<SendMsgStateEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendMsgStateEntityAction.class);

    public SendMsgStateEntityAction() {
        super(null);
    }

    public Optional<List<String>> loadNeedSyncStateSmsIds() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "loadNeedSyncStateSmsIds");
        String query_sql = getStatementFactory().getExecSql(getModelName(), "loadNeedSyncStateSmsIds", params);
        return super.queryForList(query_sql, params);
    }

    /**
     * @param store     OOXX
     * @param instances 信息实例
     */
    public String batch4MsgInit(StoEntity store, Collection<SendMsgStateEntity> instances) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(instances));
        // 多线程下的 批次号的唯一性要保证
        long _s = ThreadLocalRandom.current().nextLong(10000000L, 99999999999999L);
        String seed = Strings.padStart(String.valueOf(_s), 14, '0');
        String batchNo = String.format("%d-%d-%s", store.getCompanyId(), store.getId(), seed);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(store=%d,batchNo=%s,Collection<SendMsg4SendEntity> = %d) start", store.getId(),
                    batchNo, instances.size()));
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(batchNo);
        instances.forEach(x -> x.setSendBatchNo(batchNo));
        super.batchInsert(BATCHINSERT_SQL, 1024, instances);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(store=%d,batchNo=%s,Collection<SendMsg4SendEntity> = %d) end", store.getId(),
                    batchNo, instances.size()));
        return batchNo;
    }

    public void batchUpdate4SendState(Collection<SendMsg4SendEntity> entities) {
        String update_sql = "UPDATE SMS_TRANSPORT_LOG SET send_status= ?, send_res_code= ?, send_local_date= ?, send_remarks= ? WHERE id = ?";
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(update_sql, entities, 256, (ps, t) -> {
            ps.setObject(1, t.getSendStatus().getStatus());
            ps.setObject(2, t.getSendResCode());
            ps.setObject(3, t.getSendLocalDate());
            ps.setObject(4, t.getSendRemarks());
            ps.setObject(5, t.getId());
        });
    }

    public void batchUpdate4FinalState(Collection<SendMsg4FinalEntity> finalStates) {
        if (CollectionUtils.isEmpty(finalStates)) return;
        String update_sql = "UPDATE SMS_TRANSPORT_LOG SET final_state= ?, final_state_date = ?, final_state_desc= ? WHERE id = ?";
        super.batchInsert(update_sql, 512, finalStates);
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateFinalState() update size is %s", finalStates.size()));
    }

    public void batchUpdate4Deduction(Collection<SendMsg4DeductionEntity> deductions) {
        String update_sql = "UPDATE SMS_TRANSPORT_LOG SET send_status = ?,send_remarks = ? WHERE id = ?";
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(update_sql, deductions, 1024, (ps, et) -> {
            ps.setObject(1, et.getSendStatus().getStatus());
            ps.setObject(2, et.getRemarks());
            ps.setObject(3, et.getId());
        });
    }

    public void batchUpdate4ErrorDeduction(Collection<SendMsg4DeductionEntity> deductions, String errMsg) {
        String update_sql = "UPDATE SMS_TRANSPORT_LOG SET send_status = ?,send_remarks = ? WHERE id = ?";
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(update_sql, deductions, 1024, (ps, et) -> {
            ps.setObject(1, SendStatus.Msg4SendError);
            ps.setObject(2, errMsg);
            ps.setObject(3, et.getId());
        });
    }

    public int batchUpdate4WxMsgByBatchNo(MsgTransportBatchEntity transportBatch) {
        String update_sql = "UPDATE SMS_TRANSPORT_LOG SET send_status = 1 WHERE send_status = 0 AND send_channel = 2 AND sms_enabled = 1 AND send_batchno = ?";
        int size = Objects.requireNonNull(super.getJdbcTemplate()).update(update_sql, transportBatch.getBatchNo());
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchUpdate4WxMsgByBatchNo(%s) size is %d", transportBatch.getBatchNo(), size));
        return size;
    }

    public Optional<List<SendMsg4DeductionEntity>> loadSmsMsg4SendByBatchNo(MsgTransportBatchEntity transportBatch) {
        Map<String, Object> params = transportBatch.toParamMap();
        params.put("sql", "loadSmsMsg4SendByBatchNo");
        String query_sql = getStatementFactory().getExecSql(getModelName(), "loadSmsMsg4SendByBatchNo", params);
        List<SendMsg4DeductionEntity> list = Objects.requireNonNull(getNamedParameterJdbcTemplate())
                .query(query_sql, params, new DeductionBatchRowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSmsMsg4SendByBatchNo(%s) return list size is %d", transportBatch.getBatchNo(),
                    CollectionUtils.isEmpty(list) ? 0 : list.size()));
        return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    private final String BATCHINSERT_SQL = "INSERT INTO SMS_TRANSPORT_LOG (id, company_id, store_id, member_id, send_batchno, " +
            "phone_no, sms_count, word_count, member_name, sms_context, tenant_id, creator, free_send, sms_channel, " +
            "businsess_type, send_status, job_id, sms_enabled, send_channel, weixin_id, device_id, send_remarks) \n" +
            " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?)";

    @Override
    protected RowMapper<SendMsgStateEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SendMsgStateEntity> {
        @Override
        public SendMsgStateEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SendMsgStateEntity(res.getString("id"), res);
        }
    }

    private static class DeductionBatchRowMapperImpl implements RowMapper<SendMsg4DeductionEntity> {
        @Override
        public SendMsg4DeductionEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SendMsg4DeductionEntity(res.getString("id"), res);
        }
    }
}
