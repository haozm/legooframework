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

public class SendMsg4FinalEntityAction extends BaseEntityAction<SendMsg4FinalEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendMsg4FinalEntityAction.class);

    public SendMsg4FinalEntityAction() {
        super(null);
    }

    public Optional<List<SendMsg4FinalEntity>> load4FinalState(int size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("limit", size);
        params.put("sql", "load4Final");
        Optional<List<SendMsg4FinalEntity>> list = super.queryForEntities("load4Final", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("load4FinalState(%s) return size %s", size, list.map(List::size).orElse(0)));
        return list;
    }

    private static String BATCHUPDATE_SQL = "UPDATE SMS_TRANSPORT_LOG SET send_status = ?, final_state= ?, final_state_date = ?, final_state_desc= ? WHERE id = ?";

    public void batchUpdate(Collection<SendMsg4FinalEntity> ins) {
        if (CollectionUtils.isEmpty(ins)) return;
        super.batchInsert(BATCHUPDATE_SQL, ins.size(), ins);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchUpdate() update size is %s", ins.size()));
    }

    @Override
    protected RowMapper<SendMsg4FinalEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<SendMsg4FinalEntity> {
        @Override
        public SendMsg4FinalEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SendMsg4FinalEntity(res.getString("id"), res);
        }
    }
}
