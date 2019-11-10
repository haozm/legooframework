package com.legooframework.model.smsresult.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SMSResultEntityAction extends BaseEntityAction<SMSResultEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSResultEntityAction.class);

    public SMSResultEntityAction() {
        super(null);
    }

    private final List<SMSResultEntity> SENDING_QUEUE = Collections.synchronizedList(Lists.newArrayListWithExpectedSize(500));

    public void insert(SMSResultEntity instance) {
        String INSERT_SQL = "INSERT INTO SMS_SENDING_LOG (id, company_id, store_id, sms_channel, send_status, phone_no, sms_count," +
                " word_count, sms_context, sms_ext, tenant_id, final_state ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Objects.requireNonNull(getJdbcTemplate()).update(INSERT_SQL, instance::setValues);
        if (logger.isDebugEnabled()) logger.debug(String.format("insert(sms %s )  OK", instance.getId()));
    }

    public void batchInsert(Collection<SMSResultEntity> instances) {
        if (CollectionUtils.isEmpty(instances)) return;
        String INSERT_SQL = "INSERT INTO SMS_SENDING_LOG (id, company_id, store_id, sms_channel, send_status, phone_no, sms_count," +
                " word_count, sms_context, sms_ext, tenant_id, final_state ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(INSERT_SQL, instances, 256, (ps, instance) -> instance.setValues(ps));
        if (logger.isDebugEnabled()) logger.debug(String.format("batchInsert(sms) size is %s.", instances.size()));
    }

    /**
     * @param smsIds 短信IDS
     * @return Options
     */
    public Optional<List<SMSResultEntity>> loadByIds(Collection<String> smsIds) {
        if (CollectionUtils.isEmpty(smsIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsIds", smsIds.stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList()));
        Optional<List<SMSResultEntity>> res = super.queryForEntities("loadByIds", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByIds() size is %s", res.map(List::size).orElse(0)));
        return res;
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
