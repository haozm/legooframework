package com.legooframework.model.smsresult.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.smsprovider.service.SyncSmsDto;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SMSReplayEntityAction extends BaseEntityAction<SMSReplayEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSReplayEntityAction.class);

    public SMSReplayEntityAction() {
        super(null);
    }

    public void batchInsert(SyncSmsDto syncSmsDto) {
        if (!syncSmsDto.getResponse().isPresent()) return;
        String replay_str = syncSmsDto.getResponse().get();
        String[] args = StringUtils.split(replay_str, "|||");
        if (ArrayUtils.isEmpty(args)) return;
        List<SMSReplayEntity> list = Lists.newArrayList();
        Stream.of(args).forEach(x -> list.add(SMSReplayEntity.createInstance(syncSmsDto.getAccount(), x)));
        super.batchInsert("batchInsert", list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(SMSReplayEntity) size is %s", list.size()));
    }

    private synchronized DateTime[] getLastReplayTime() {
        String query_sql = "SELECT createTime FROM SMS_LAST_SYNC_DATE where sync_type='SYNC_SMS_REPLAY' ORDER BY id DESC LIMIT 1";
        DateTime start_date = new DateTime(getJdbcTemplate().queryForObject(query_sql, Date.class));
        final DateTime end_date = DateTime.now();
        String insert_sql = "INSERT INTO SMS_LAST_SYNC_DATE (sync_type, createTime) VALUES('SYNC_SMS_REPLAY', ?)";
        getJdbcTemplate().update(insert_sql, ps -> ps.setObject(1, end_date.toDate()));
        return new DateTime[]{start_date, end_date};
    }

    public Optional<List<SMSReplayEntity>> load4TDEntities() {
        DateTime[] dates = getLastReplayTime();
        Map<String, Object> params = Maps.newHashMap();
        params.put("start", dates[0].plusMinutes(-5).toDate());
        params.put("end", dates[1].toDate());
        Optional<List<SMSReplayEntity>> list = super.queryForEntities("load4TDEntities", params, new BlackRowMapperImpl());
        if (list.isPresent() && logger.isDebugEnabled())
            logger.debug(String.format("load4TDEntities() res is %s", list));
        return list;
    }

    @Override
    protected RowMapper<SMSReplayEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SMSReplayEntity> {
        @Override
        public SMSReplayEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSReplayEntity(res.getLong("id"), res);
        }
    }

    private static class BlackRowMapperImpl implements RowMapper<SMSReplayEntity> {
        @Override
        public SMSReplayEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSReplayEntity(res);
        }
    }
}
