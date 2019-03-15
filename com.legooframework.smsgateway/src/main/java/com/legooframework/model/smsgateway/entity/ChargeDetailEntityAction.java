package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ChargeDetailEntityAction extends BaseEntityAction<ChargeDetailEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ChargeDetailEntityAction.class);

    public ChargeDetailEntityAction() {
        super(null);
    }

    public void batchInsert(Collection<ChargeDetailEntity> billingDetails) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(billingDetails));
        super.batchInsert("batchInsert", billingDetails);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(...) size is %s", billingDetails.size()));
    }

    public Optional<ChargeDetailEntity> loadByBatchNo(String batchNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(batchNo));
        Map<String, Object> params = Maps.newHashMap();
        params.put("batchNo", batchNo);
        Optional<ChargeDetailEntity> billDetail = super.queryForEntity("loadByBatchNo", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByBatchNo(%s) return %s", batchNo, billDetail.orElse(null)));
        return billDetail;
    }

    @Override
    protected RowMapper<ChargeDetailEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<ChargeDetailEntity> {
        @Override
        public ChargeDetailEntity mapRow(ResultSet res, int i) throws SQLException {
            return new ChargeDetailEntity(res.getString("id"), res);
        }
    }
}
