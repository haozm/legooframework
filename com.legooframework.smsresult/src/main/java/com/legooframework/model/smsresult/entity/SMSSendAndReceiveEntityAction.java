package com.legooframework.model.smsresult.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.smsgateway.entity.SendStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SMSSendAndReceiveEntityAction extends BaseEntityAction<SMSSendAndReceiveEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSSendAndReceiveEntityAction.class);

    private final String INSERT_SQL =
            "INSERT INTO SMS_SENDING_LOG (id, company_id, store_id, sms_channel, send_status, phone_no, sms_count," +
                    " word_count, sms_context, sms_ext, tenant_id, final_state ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    public SMSSendAndReceiveEntityAction() {
        super(null);
    }

    private final List<SMSSendAndReceiveEntity> SENDING_QUEUE = Collections.synchronizedList(Lists.newArrayListWithExpectedSize(500));

    public synchronized void add4Insert(SMSSendAndReceiveEntity instance) {
        this.SENDING_QUEUE.add(instance);
        if (logger.isDebugEnabled())
            logger.debug(String.format("add4Insert(sms) size is %s", SENDING_QUEUE.size()));
    }

    public synchronized void batchInsert() {
        if (CollectionUtils.isEmpty(this.SENDING_QUEUE)) return;
        List<SMSSendAndReceiveEntity> list = Lists.newArrayList(this.SENDING_QUEUE);
        this.SENDING_QUEUE.clear();
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(INSERT_SQL, list, list.size(), (ps, val) -> {
            ps.setObject(1, val.getId());
            ps.setObject(2, val.getCompanyId());
            ps.setObject(3, val.getStoreId());
            ps.setObject(4, val.getSmsChannel().getChannel());
            ps.setObject(5, val.getSendStatus().getStatus());
            ps.setObject(6, val.getMobile());
            ps.setObject(7, val.getSendSms().getSmsNum());
            ps.setObject(8, val.getSendSms().getWordCount());
            ps.setObject(9, val.getSendSms().getContent());
            ps.setObject(10, val.getSmsExt());
            ps.setObject(11, val.getCompanyId());
            ps.setObject(12, val.getFinalState().getState());
        });
        if (logger.isDebugEnabled()) logger.debug(String.format("batchInsert(sms) size is %s.", list.size()));
    }

    /**
     * @param smsIds 短信IDS
     * @return Options
     */
    public Optional<List<SMSSendAndReceiveEntity>> loadByIds(Collection<String> smsIds) {
        if (CollectionUtils.isEmpty(smsIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsIds", smsIds.stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList()));
        Optional<List<SMSSendAndReceiveEntity>> res = super.queryForEntities("loadByIds", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByIds() size is %s", res.map(List::size).orElse(0)));
        return res;
    }

    @Override
    protected RowMapper<SMSSendAndReceiveEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SMSSendAndReceiveEntity> {
        @Override
        public SMSSendAndReceiveEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSSendAndReceiveEntity(res.getString("id"), res);
        }
    }

}
