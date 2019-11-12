package com.legooframework.model.smsresult.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SMSResultEntityAction extends BaseEntityAction<SMSResultEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSResultEntityAction.class);

    public SMSResultEntityAction() {
        super(null);
    }

    public void insert(SMSResultEntity instance) {
        String INSERT_SQL = "INSERT INTO SMS_SENDING_LOG (id, company_id, store_id, sms_channel, send_status, phone_no, sms_count," +
                " word_count, sms_context, sms_ext, tenant_id, final_state ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Objects.requireNonNull(getJdbcTemplate()).update(INSERT_SQL, instance::setValues);
        if (logger.isDebugEnabled()) logger.debug(String.format("insert(sms %s )  OK", instance.getId()));
    }

    /**
     * 批量插入
     *
     * @param instances OOXX
     */
    public void batchInsert(Collection<SMSResultEntity> instances) {
        if (CollectionUtils.isEmpty(instances)) return;
        String INSERT_SQL = "INSERT INTO SMS_SENDING_LOG (id, company_id, store_id, sms_channel, send_status, phone_no, sms_count, \n" +
                " word_count, sms_context, sms_ext, tenant_id, final_state ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(INSERT_SQL, instances, 256, (ps, instance) -> instance.setValues(ps));
        if (logger.isDebugEnabled()) logger.debug(String.format("batchInsert(sms) size is %s.", instances.size()));
    }

    /**
     * @param smsIds 短信IDS
     * @return Options OOXX
     */
    public Optional<List<SMSResultEntity>> loadByIds(Collection<String> smsIds) {
        if (CollectionUtils.isEmpty(smsIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsIds", smsIds.stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList()));
        params.put("sql", "loadByIds");
        Optional<List<SMSResultEntity>> res = super.queryForEntities("query4list", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByIds() size is %s", res.map(List::size).orElse(0)));
        return res;
    }

    public Optional<List<Map<String, Object>>> load4SyncState(int start, int end) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", "load4SyncState");
        params.put("start", start);
        params.put("end", end);
        return super.queryForMapList("load4SyncState", params);
    }

    public void updateState(Collection<Map<String, Object>> statusMaps) {
        if (CollectionUtils.isEmpty(statusMaps)) return;
        String update_sql = "UPDATE SMS_SENDING_LOG SET final_state = ?, final_state_date = ?, final_state_desc = ? WHERE phone_no = ? AND send_msg_id = ? AND final_state = 98";
        int[][] size = Objects.requireNonNull(getJdbcTemplate()).batchUpdate(update_sql, statusMaps, 512, (ps, map) -> {
            ps.setObject(1, MapUtils.getInteger(map, "finalState"));
            ps.setObject(2, MapUtils.getObject(map, "finalStateDate"));
            ps.setObject(3, MapUtils.getString(map, "finalStateDesc"));
            ps.setObject(4, MapUtils.getString(map, "phoneNo"));
            ps.setObject(5, MapUtils.getLong(map, "sendMsgId"));
        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateState() retuen batch is %s", size.length));
    }

    @Override
    protected RowMapper<SMSResultEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SMSResultEntity> {
        @Override
        public SMSResultEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSResultEntity(res.getString("id"), res);
        }
    }

}
