package com.legooframework.model.smsgateway.entity;

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

public class SendMsg4ReimburseEntityAction extends BaseEntityAction<SendMsg4ReimburseEntity> {
    private static final Logger logger = LoggerFactory.getLogger(SendMsg4ReimburseEntityAction.class);

    public SendMsg4ReimburseEntityAction() {
        super(null);
    }

    public void updateReimburseState(ReimburseResDto reimburses) {
        if (reimburses.isEmpty()) return;
        List<String> ids = reimburses.getSmsIds().stream().map(x -> String.format("'%s'", x)).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        params.put("batchNo", reimburses.getBatchNo());
        super.updateAction("updateReimburseState", params);
    }

    public Optional<List<ReimburseResDto>> loadUnReimburseDto() {
        String query_sql = "SELECT stl.company_id,stl.store_id,sum(stl.sms_count) AS 'totalSmsCount', GROUP_CONCAT(stl.id)  AS 'smsIds' " +
                "FROM acp.SMS_TRANSPORT_LOG stl " +
                "WHERE stl.send_status =4 AND stl.send_channel =1 AND stl.reimburse_state = 0 GROUP BY stl.company_id,stl.store_id";
        List<ReimburseResDto> dtos = Objects.requireNonNull(super.getJdbcTemplate()).query(query_sql, new ReimburseDtoRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadUnReimburseDtos() size is %d", CollectionUtils.isEmpty(dtos) ? 0 : dtos.size()));
        return Optional.ofNullable(CollectionUtils.isEmpty(dtos) ? null : dtos);
    }


    @Override
    protected RowMapper<SendMsg4ReimburseEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SendMsg4ReimburseEntity> {
        @Override
        public SendMsg4ReimburseEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SendMsg4ReimburseEntity(res.getString("id"), res);
        }
    }

    private static class ReimburseDtoRowMapper implements RowMapper<ReimburseResDto> {
        @Override
        public ReimburseResDto mapRow(ResultSet res, int i) throws SQLException {
            return new ReimburseResDto(res);
        }
    }
}
