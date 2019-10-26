package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.ToIntFunction;

public class ChargeDetailEntityAction extends BaseEntityAction<ChargeDetailEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ChargeDetailEntityAction.class);

    private static final Comparator<ChargeDetailEntity> comparable = Comparator
            .comparingInt((ToIntFunction<ChargeDetailEntity>) detail -> detail.getSmsBatchNo().hashCode())
            .thenComparingInt(detail -> detail.getRechargeScope().getScope());

    public ChargeDetailEntityAction() {
        super(null);
    }

    public void batchInsert(Collection<ChargeDetailEntity> billingDetails) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(billingDetails));
        super.batchInsert("batchInsert", billingDetails);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(...) size is %s", billingDetails.size()));
    }

    /**
     * 加载一批指定 发送批次号码对应的扣款明细
     *
     * @param smsBatchNos 发送批次号
     * @return
     */
    public Multimap<String, ChargeDetailEntity> loadBySmsBatchNos(Collection<String> smsBatchNos) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(smsBatchNos));
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsBatchNos", smsBatchNos);
        Optional<List<ChargeDetailEntity>> billDetails = super.queryForEntities("loadBySmsBatchNos", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBySmsBatchNos(%s) return %s", smsBatchNos, billDetails.orElse(null)));
        Preconditions.checkState(billDetails.isPresent());
        billDetails.get().sort(comparable);
        ArrayListMultimap<String, ChargeDetailEntity> multimap = ArrayListMultimap.create();
        billDetails.get().forEach(x -> multimap.put(x.getSmsBatchNo(), x));
        return multimap;
    }

    /**
     * 加载一批指定 发送批次号码对应的扣款明细
     *
     * @param smsBatchNo 发送批次号
     * @return
     */
    public List<ChargeDetailEntity> loadBySmsBatchNo(String smsBatchNo) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsBatchNos", new String[]{smsBatchNo});
        Optional<List<ChargeDetailEntity>> billDetails = super.queryForEntities("loadBySmsBatchNos", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBySmsBatchNo(%s) return %s", smsBatchNo, billDetails.orElse(null)));
        Preconditions.checkState(billDetails.isPresent());
        billDetails.get().sort(comparable);
        return billDetails.get();
    }

    public void batchWriteOff(Collection<ChargeDetailEntity> chargeDetails) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(chargeDetails));
        super.batchUpdate("batchWriteOff", (ps, chargeDetail) -> {
            ps.setObject(1, chargeDetail.getReimburseNum());
            ps.setObject(2, chargeDetail.getId());
        }, chargeDetails);
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
