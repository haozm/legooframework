package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SendMsg4ReimburseEntityAction extends BaseEntityAction<SendMsg4ReimburseEntity> {
    private static final Logger logger = LoggerFactory.getLogger(SendMsg4ReimburseEntityAction.class);

    public SendMsg4ReimburseEntityAction() {
        super(null);
    }

    public Optional<List<SendMsg4ReimburseEntity>> loadBySendBatchNo(String sendBatchNo) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sendBatchNo", sendBatchNo);
        Optional<List<SendMsg4ReimburseEntity>> resultset = super.queryForEntities("load4Reimburse", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBySendBatchNo(%s) list size is %s", sendBatchNo, resultset.map(List::size).orElse(0)));
        return resultset;
    }

    public int batchReimburse(Collection<SendMsg4ReimburseEntity> reimburses) {
        if (CollectionUtils.isEmpty(reimburses)) return 0;
        List<String> ids = reimburses.stream().map(x -> String.format("'%s'", x.getId())).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", ids);
        return super.updateAction("update4Reimburse", params);
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
}
